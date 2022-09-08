package ua.com.fielden.platform.gis.gps.actors.impl;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;

import java.math.BigDecimal;
import java.util.Collection;

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
    private final static int ignitionOffTimeout1_5 = 60 /* ignitionOffTimeout */ * 3 / 2 /* multiplier */ * 1000 /* millis in second */;

    public static void createJourneysFrom(final Collection<TgMessage> messages, final TgMachine machine, final TgJourneyCo journeyCo, final ITgMachineModuleAssociation machineModuleAssociationCo, final TgMachineDriverAssociationCo machineDriverAssociationCo) {
        messages.stream().filter(
            // trip start
            message -> message.isTrip()
            // trip finish
            || !message.getIgnition()
               && message.getVectorSpeed() != null && message.getVectorSpeed() >= 0 && message.getVectorSpeed() <= 5
               && message.getTripOdometer() != null && message.getTripOdometer() > 0
       ).forEach(message -> {
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
                        .and().prop("earliestDate").gt().val(message.getGpsTime())
                        .model()
                    )
                    .with(TgJourneyCo.FETCH_PROVIDER.fetchModel())
                    .with(orderBy().prop("earliestDate").asc().model()).model(); // take only one closest intersecting interval i.e. sort by 'earliestDate' ascending and getFirstEntities(..., 1)
                final var journeyOpt = journeyCo.getFirstEntities(qem, 1).stream().findAny();
                final var targetJourney =
                    // if there are no earliest period or ...
                    !journeyOpt.isPresent()
                    // ... if earliest period is closed ...
                    || journeyOpt.get().getStartDate() != null
                    // ... then create new open period with start;
                    ? createJourney(driver, machine, journeyCo)
                    // otherwise update start for the earliest period
                    : journeyOpt.get();
                journeyCo.save(updateInfo("start", targetJourney, message, initOdometer, address));
            } else { // trip odometer > 0, ignition OFF and speed in [0; 5] -- journey finish detected
                final var qem = 
                    from(select(TgJourney.class).where()
                        .prop("machine").eq().val(machine)
                        .and().prop("latestDate").lt().val(message.getGpsTime())
                        .model()
                    )
                    .with(TgJourneyCo.FETCH_PROVIDER.fetchModel())
                    .with(orderBy().prop("latestDate").desc().model()).model(); // take only one closest intersecting interval i.e. sort by 'latestDate' descending and getFirstEntities(..., 1)
                final var journeyOpt = journeyCo.getFirstEntities(qem, 1).stream().findAny();
                
                final var targetJourney =
                    // if there are no latest period or ...
                    !journeyOpt.isPresent()
                    // ... if latest period is closed and delta > 90 seconds ...
                    || journeyOpt.get().getFinishDate() != null && !journeyOpt.get().isPreliminaryFinish() && message.getGpsTime().getTime() - journeyOpt.get().getFinishDate().getTime() > ignitionOffTimeout1_5
                    // ... then create new open period with preliminary finish;
                    ? createJourney(driver, machine, journeyCo).setPreliminaryFinish(true)
                    // otherwise update finish for the latest period;
                    // if it was open then make finish preliminary;
                    // also make it preliminary if outside ignition OFF timeout
                    : journeyOpt.get().setPreliminaryFinish(journeyOpt.get().getFinishDate() == null || message.getGpsTime().getTime() - journeyOpt.get().getFinishDate().getTime() > ignitionOffTimeout1_5);
                journeyCo.save(updateInfo("finish", targetJourney, message, initOdometer, address));
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

    private static String reverseGeocode(final BigDecimal y, final BigDecimal x) {
        return "SAMPLE"; // TODO
    }

}