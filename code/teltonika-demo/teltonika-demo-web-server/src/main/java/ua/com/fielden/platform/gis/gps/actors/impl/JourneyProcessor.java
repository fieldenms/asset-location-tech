package ua.com.fielden.platform.gis.gps.actors.impl;

import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.DateTime;

import fielden.personnel.Person;
import ua.com.fielden.platform.sample.domain.ITgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgJourney;
import ua.com.fielden.platform.sample.domain.TgJourneyCo;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineDriverAssociation;
import ua.com.fielden.platform.sample.domain.TgMachineDriverAssociationCo;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgMessage;

public class JourneyProcessor {

    public static void createJourneysFrom(final Collection<TgMessage> messages, final TgMachine machine, final TgJourneyCo journeyCo, final ITgMachineModuleAssociation machineModuleAssociationCo, final TgMachineDriverAssociationCo machineDriverAssociationCo) {
        messages.stream().filter(message -> message.isTrip() || !message.getIgnition() && message.getVectorSpeed() == 0 && message.getTripOdometer() > 0).forEach(message -> {
            final var initOdometer = machineModuleAssociationCo
                .getEntityOptional(
                    from(select(TgMachineModuleAssociation.class).where()
                        .prop("machine").eq().val(machine)
                        .and().prop("from").le().val(message.getGpsTime())
                        .and().begin().prop("to").isNull().or().prop("to").gt().val(message.getGpsTime()).end()
                        .model()
                    )
                    .with(fetchWithKeyAndDesc(TgMachineModuleAssociation.class).with("initOdometer").fetchModel())
                    .model()
                ) // only one instance is expected (if history is correct; also note that 'messages' got sent to this machine actor, that means that machineModuleAssociation should exist for that machine for all gpsTimes)
                .map(TgMachineModuleAssociation::getInitOdometer)
                .orElse(0);
            final var driver = machineDriverAssociationCo
                .getEntityOptional(
                    from(select(TgMachineDriverAssociation.class).where()
                        .prop("machine").eq().val(machine)
                        .and().prop("from").le().val(message.getGpsTime())
                        .and().begin().prop("to").isNull().or().prop("to").gt().val(message.getGpsTime()).end()
                        .model()
                    )
                    .with(fetchWithKeyAndDesc(TgMachineDriverAssociation.class).with("driver").fetchModel())
                    .model()
                ) // only one instance is expected (if history is correct)
                .map(TgMachineDriverAssociation::getDriver)
                .orElse(null);

            final var address = reverseGeocode(message.getY() /*lat*/, message.getX() /*long*/);

            if (message.isTrip()) { // journey start detected
                final var qem = 
                    from(select(TgJourney.class).where()
                        .prop("machine").eq().val(machine)
                        .and().prop("finishDate").isNotNull()
                        .and().prop("finishDate").gt().val(message.getGpsTime())
                        .and().begin().prop("startDate").isNull().or().prop("startDate").lt().val(message.getGpsTime()).end() // don't care about journey that started at message.getGpsTime() i.e. use lt() not le(); this is unlikely and, if happened, will just leave existing journey "as is"
                        .model()
                    )
                    .with(TgJourneyCo.FETCH_PROVIDER.fetchModel())
                    .with(orderBy().prop("finishDate").asc().model()).model(); // take only one closest intersecting interval i.e. sort by 'finishDate' ascending and getFirstEntities(..., 1)
                final var journeyOpt = journeyCo.getFirstEntities(qem, 1).stream().findAny();
                if (journeyOpt.isPresent()) {
                    final var intersectedJourney = journeyOpt.get();
                    final var newJourneyOpt = ofNullable(intersectedJourney.getStartDate()).map(sd -> clearInfo("finish", intersectedJourney.copyTo(journeyCo.new_())));
                    journeyCo.save(updateInfo("start", intersectedJourney, message, initOdometer, address));
                    newJourneyOpt.ifPresent(newJourney -> journeyCo.save(newJourney));
                } else {
                    journeyCo.save(updateInfo("start", createJourney(driver, machine, journeyCo), message, initOdometer, address));
                }
            } else { // trip odometer > 0, ignition OFF and speed = 0 -- journey finish detected
                final var ignitionOffTimeout1_5 = 60 /* ignitionOffTimeout */ * 3 / 2 /* multiplier */ * 1000 /* millis in second */;
                final var qem = 
                    from(select(TgJourney.class).where()
                        .prop("machine").eq().val(machine)
                        .and().prop("startDate").isNotNull()
                        .and().prop("startDate").lt().val(message.getGpsTime())
                        .and().begin().prop("finishDate").isNull().or().prop("finishDate").gt().val(new DateTime(message.getGpsTime()).minus(ignitionOffTimeout1_5).toDate()).end() // don't care about journey that finished at message.getGpsTime() i.e. use gt() not ge(); this is unlikely and, if happened, will just leave existing journey "as is"
                        .model()
                    )
                    .with(TgJourneyCo.FETCH_PROVIDER.fetchModel())
                    .with(orderBy().prop("startDate").desc().model()).model(); // take only one closest intersecting interval i.e. sort by 'startDate' descending and getFirstEntities(..., 1)
                final var journeyOpt = journeyCo.getFirstEntities(qem, 1).stream().findAny();
                if (journeyOpt.isPresent()) {
                    final var intersectedJourney = journeyOpt.get();
                    if (intersectedJourney.getFinishDate() != null && intersectedJourney.getFinishDate().getTime() - message.getGpsTime().getTime() > ignitionOffTimeout1_5) {
                        final var newJourney = clearInfo("start", intersectedJourney.copyTo(journeyCo.new_()));
                        journeyCo.save(updateInfo("finish", intersectedJourney, message, initOdometer, address));
                        journeyCo.save(newJourney);
                    } else {
                        journeyCo.save(updateInfo("finish", intersectedJourney, message, initOdometer, address));
                    }
                } else {
                    journeyCo.save(updateInfo("finish", createJourney(driver, machine, journeyCo), message, initOdometer, address)); // TODO in this case we can have two open periods with finishDate that are close enough (within ignitionOffTimeout) -- cover this edge-case too
                }
            }
        });
    }

    private static TgJourney createJourney(final Person driver, final TgMachine machine, final TgJourneyCo journeyCo) {
        return journeyCo.new_()
            .setDriver(driver)
            .setMachine(machine);
    }

    private static TgJourney updateInfo(final String propPrefix, final TgJourney journey, final TgMessage message, final Integer initOdometer, final String address) {
        return (TgJourney) journey
            .set(propPrefix + "Date", message.getGpsTime())
            .set(propPrefix + "Odometer", initOdometer + message.getTotalOdometer() / 1000)
            .set(propPrefix + "Address", address)
            .set(propPrefix + "Latitude", message.getY())
            .set(propPrefix + "Longitude", message.getX());
    }

    private static TgJourney clearInfo(final String propPrefix, final TgJourney journey) {
        return (TgJourney) journey
            .set(propPrefix + "Date", null)
            .set(propPrefix + "Odometer", null)
            .set(propPrefix + "Address", null)
            .set(propPrefix + "Latitude", null)
            .set(propPrefix + "Longitude", null);
    }

    private static String reverseGeocode(final BigDecimal y, final BigDecimal x) {
        return "SAMPLE"; // TODO
    }

}