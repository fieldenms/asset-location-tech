package ua.com.fielden.platform.gis.gps.factory.tmp;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.Logger;

import fielden.teltonika.AvlData;
import fielden.teltonika.AvlGpsElement;
import fielden.teltonika.IAvlTrackerHandler;

public class AvlTrackerLoggingHandler implements IAvlTrackerHandler {
    private final int coordPropScale = 10;
    private final Logger log = getLogger(AvlTrackerLoggingHandler.class);

    @Override
    public void handleData(final String imei, final AvlData[] data) {
        for (int index = 0; index < data.length; index++) {
            final AvlData value = data[index];
            final AvlGpsElement gps = value.getGps();
            log.info("GPS[" + index + "] = " + gps.getLatitude(coordPropScale) + ", " + gps.getLongitude(coordPropScale));
            log.debug("\tNMEA recreate:");
            log.debug("\t" + createGga(value.getTimestamp(), gps));
            log.debug("\t" + createRmc(value.getTimestamp(), gps));
        }
    }

    private String createGga(final long timestamp, final AvlGpsElement gps) {
        final Date date = new Date(timestamp);

        final StringBuilder nmea = new StringBuilder("$GPGGA,") //
                .append(new SimpleDateFormat("HHmmss").format(date) + ",") //
                .append(Math.abs(gps.getLatitude(coordPropScale).doubleValue()) + ",") //
                .append((gps.getLatitude(coordPropScale).doubleValue() < 0) ? "S," : "N,") //
                .append(Math.abs(gps.getLongitude(coordPropScale).doubleValue()) + ",") //
                .append((gps.getLongitude(coordPropScale).doubleValue() < 0) ? "W," : "E,") //
                .append("1,") //
                .append(gps.getSatellites() + ",") //
                .append("0.0,") //
                .append(gps.getAltitude() + ".0,") //
                .append("M,") //
                .append("0.0,") //
                .append("M,") //
                .append(",") //
                .append("*00");

        return nmea.toString();
    }

    private String createRmc(final long timestamp, final AvlGpsElement gps) {
        final Date date = new Date(timestamp);

        final StringBuilder nmea = new StringBuilder("$GPRMC,").append(new SimpleDateFormat("HHmmss").format(date) + ",").append("A,").append(Math.abs(gps.getLatitude(coordPropScale).doubleValue())
                + ",").append((gps.getLatitude(coordPropScale).doubleValue() < 0) ? "S,"
                        : "N,").append(Math.abs(gps.getLongitude(coordPropScale).doubleValue()) + ",").append((gps.getLongitude(coordPropScale).doubleValue() < 0) ? "W," : "E,").append(gps.getSpeed() + ",").append(gps.getAngle()
                                + ".0,").append(new SimpleDateFormat("ddMMyy").format(date)
                                        + ",").append(",,A*6F");

        return nmea.toString();
    }

    @Override
    public boolean authorise(final String imei) {
        return true;
    }

}
