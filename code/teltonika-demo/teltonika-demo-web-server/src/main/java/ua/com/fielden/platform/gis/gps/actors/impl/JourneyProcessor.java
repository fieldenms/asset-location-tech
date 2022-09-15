package ua.com.fielden.platform.gis.gps.actors.impl;

import static java.lang.String.format;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpRequest.newBuilder;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.EntityUtils.fetchWithKeyAndDesc;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

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
            message -> isJourneyStart(message) || isJourneyFinish(message) || isJourneyFinishCausedByGnssOutage(message)
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

            final var address = reverseGeocode(message.getY().toPlainString() /*lat*/, message.getX().toPlainString() /*long*/);

            if (isJourneyStart(message)) { // journey start detected
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
            } else { // journey finish detected (maybe caused by GNSS outage)
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
                    : journeyOpt.get().setPreliminaryFinish(!isJourneyFinishCausedByGnssOutage(message) && (journeyOpt.get().getFinishDate() == null || message.getGpsTime().getTime() - journeyOpt.get().getFinishDate().getTime() > ignitionOffTimeout1_5))
                        .setGnssOutageFinish(isJourneyFinishCausedByGnssOutage(message));
                if (targetJourney.isPersisted() || !isJourneyFinishCausedByGnssOutage(message)) {
                    journeyCo.save(updateInfo("finish", targetJourney, message, initOdometer, address));
                }
            }
        });
    }

    /**
     * Returns {@code true} in case where {@code message} represents {@link TgJourney} start as by definition from Teltonika tracker 'Trip' feature.
     * <p>
     * "<i>Trip</i> starts when Ignition according <i>Ignition source</i> is ON and Movement according <i>Movement source</i> is ON and also 'Start Speed' is exceeded.
     * <i>Start Speed</i> defines the minimum GPS speed in order to detect <i>Trip</i> start."
     * <p>
     * We fully rely on this (tracker sent parameter) to define journey start dates.
     * 
     * @param message
     * @see https://wiki.teltonika-gps.com/view/FMB120_Trip/Odometer_settings
     * @return
     */
    private static boolean isJourneyStart(final TgMessage message) {
        return message.isTrip();
    }

    /**
     * Returns {@code true} in case where {@code message} represents {@link TgJourney} finish as by definition from Teltonika tracker 'Trip' feature.
     * <p>
     * "<i>Ignition OFF Timeout</i> is the timeout value to detect <i>Trip</i> end once the Ignition (configured ignition source) is off.<br>
     * I/O Trip Odometer must be enabled to use <i>Distance counting mode</i> feature. When it is set to Continuous, <i>Trip</i> distance is going to be counted continuously (from <i>Trip</i> start to <i>Trip</i> end)
     * and written to I/O <i>Trip Odometer</i> value field. When <i>Trip</i> is over and the next <i>Trip</i> begins, <i>Trip Odometer</i> value is reset to zero."
     * <p>
     * We manually define criteria, that is close enough to real world data and Teltonika documentation, to define journey finish dates.
     * 
     * @param message
     * @see https://wiki.teltonika-gps.com/view/FMB120_Trip/Odometer_settings
     * @return
     */
    private static boolean isJourneyFinish(final TgMessage message) {
        return !message.getIgnition() // ignition OFF is base condition
           && message.getVectorSpeed() != null && message.getVectorSpeed() >= 0 && message.getVectorSpeed() <= 5 // sometimes speed is greater that zero but still low; we take such messages to increase the chance of always getting "advertised" two (or more) messages within Ignition OFF timeout
           && message.getTripOdometer() != null && message.getTripOdometer() > 0; // trip odometer must have `Continuous` mode enabled and, when Trip, it is always > 0
    }

    /**
     * Returns {@code true} in case where {@code message} represents sudden loss of GNSS connection with zero tripOdometer and Ignition ON.
     * This message, probably, finishes some journey.
     * <p>
     * We only finishes some journey using this message if it exists, i.e. has start.
     * We do not create open journey with finish if there are no corresponding 'In Progress?' = true journey with start.
     * This is because in some very rare cases, e.g. in garage with bad connection, we can have situation like this but trip has not started yet.
     * 
     * @param message
     * @see https://wiki.teltonika-gps.com/view/FMB120_Trip/Odometer_settings
     * @return
     */
    private static boolean isJourneyFinishCausedByGnssOutage(final TgMessage message) {
        return message.getIgnition()
          && message.getTripOdometer() != null && message.getTripOdometer() == 0
          && message.getVisibleSattelites() != null && message.getVisibleSattelites() == 0;
    }

    private static TgJourney createJourney(final Person driver, final TgMachine machine, final TgJourneyCo journeyCo) {
        return journeyCo.new_()
            .setDriver(driver)
            .setMachine(machine);
    }

    private static TgJourney updateInfo(final String propPrefix, final TgJourney journey, final TgMessage message, final Integer initOdometer, final String address) {
        return (TgJourney) journey
            .set(propPrefix + "Date", message.getGpsTime())
            .set(propPrefix + "Odometer", valueOf(initOdometer).setScale(2).add(
                valueOf(message.getTotalOdometer()).setScale(2).divide(valueOf(1000), HALF_UP))
            )
            .set(propPrefix + "Address", address)
            .set(propPrefix + "Latitude", message.getY())
            .set(propPrefix + "Longitude", message.getX());
    }

    private static String reverseGeocode(final String latitude, final String longitude) {
        try {
            final HttpResponse<String> response = newHttpClient().send(newBuilder(new URI(format("https://nominatim.openstreetmap.org/reverse?lat=%s&lon=%s&format=json", latitude, longitude))).GET().build(), BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                System.out.println(response.body());
                final ObjectMapper mapper = new ObjectMapper();
                final Map map = mapper.readValue(response.body(), Map.class);
                if (map.get("display_name") != null && map.get("display_name") instanceof String) {
                    System.out.println((String) map.get("display_name"));
                    return (String) map.get("display_name");
                }
            }
            return "unknown location";
        } catch (final URISyntaxException | InterruptedException | IOException e) {
            e.printStackTrace();
            return "unknown location (error)";
        }
    }

    public static void main(final String[] args) {
        reverseGeocode("49.8550266000", "24.0234399000");
    }

}