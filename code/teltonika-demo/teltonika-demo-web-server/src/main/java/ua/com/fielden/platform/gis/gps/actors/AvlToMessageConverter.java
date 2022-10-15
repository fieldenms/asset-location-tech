package ua.com.fielden.platform.gis.gps.actors;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static org.apache.logging.log4j.LogManager.getLogger;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.AvlData;
import ua.com.fielden.platform.gis.gps.AvlGpsElement;
import ua.com.fielden.platform.gis.gps.AvlIoCodes;
import ua.com.fielden.platform.gis.gps.AvlIoElement;

/**
 * A convenient routine for populating data from {@link AvlData} to GPS message.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public class AvlToMessageConverter<T extends AbstractAvlMessage> {

    protected static final Logger LOGGER = getLogger(AvlToMessageConverter.class);

    /** A convenient routine for populating data from {@link AvlData} to GPS message. */
    public T populateData(final T msg, final AvlData avl, final Date packetReceived) {
        final AvlGpsElement gps = avl.getGps();
        final Date gpsTime = new Date(avl.getGpsTimestamp());

        msg.setAltitude(Integer.valueOf(gps.getAltitude()));
        msg.setX(gps.getLongitude());
        msg.setY(gps.getLatitude());
        msg.setVectorSpeed(Integer.valueOf(gps.getSpeed()));
        msg.setVectorAngle(Integer.valueOf(gps.getAngle()));
        msg.setVisibleSattelites(Integer.valueOf(gps.getSatellites()));
        msg.setGpsTime(gpsTime);
        msg.setDin1(locateDin1(avl.getIo()));
        msg.setPowerSupplyVoltage(locatePowerSupplyVot(avl.getIo()));
        msg.setBatteryVoltage(locateBatteryVot(avl.getIo()));
        msg.setGpsPower(locateGpsPower(avl.getIo()));
        msg.setPacketReceived(packetReceived);
        msg.setIgnition(locateIgnition(avl.getIo()));
        msg.setTotalOdometer(locateTotalOdometer(avl.getIo()));
        msg.setTripOdometer(locateTripOdometer(avl.getIo()));
        msg.setTrip(locateTrip(avl.getIo()));

        return msg;
    }

    protected static void printIo(final AvlIoElement io) {
        LOGGER.info("Event ID == " + io.eventId);
        LOGGER.info("\tbyte IO length == " + io.byteIo.length);
        for (int index = 0; index < io.byteIo.length; index++) {
            LOGGER.info("\t\t" + io.byteIo[index]);
        }

        LOGGER.info("\tshort IO length == " + io.shortIo.length);
        for (int index = 0; index < io.shortIo.length; index++) {
            LOGGER.info("\t\t" + io.shortIo[index]);
        }

        LOGGER.info("\tint IO length == " + io.intIo.length);
        for (int index = 0; index < io.intIo.length; index++) {
            LOGGER.info("\t\t" + io.intIo[index]);
        }

        LOGGER.info("\tlong IO length == " + io.longIo.length);
        for (int index = 0; index < io.longIo.length; index++) {
            LOGGER.info("\t\t" + io.longIo[index]);
        }
    }

    //    private static BigDecimal locateOdometer(final AvlIoElement io) {
    //	for (int index = 0; index < io.intIo.length; index++) {
    //	    if (io.intIo[index].ioId == AvlIoCodes.VIRT_ODOMETER.id) {
    //		return new BigDecimal(io.intIo[index].ioValue);
    //	    }
    //	}
    //	return null;
    //    }

    private static boolean locateDin1(final AvlIoElement io) {
        for (int index = 0; index < io.byteIo.length; index++) {
            if (io.byteIo[index].ioId == AvlIoCodes.DIN1.id) {
                return io.byteIo[index].ioValue == 1;
            }
        }
        return false;
    }

    private static BigDecimal locatePowerSupplyVot(final AvlIoElement io) {
        for (int index = 0; index < io.shortIo.length; index++) {
            if (io.shortIo[index].ioId == AvlIoCodes.POWER_SUPPLY_VOLT.id) {
                return voltageFrom(io.shortIo[index].ioValue);
            }
        }
        return null;
    }

    private static BigDecimal locateBatteryVot(final AvlIoElement io) {
        for (int index = 0; index < io.shortIo.length; index++) {
            if (io.shortIo[index].ioId == AvlIoCodes.BATTERY_VOLT.id) {
                return voltageFrom(io.shortIo[index].ioValue);
            }
        }
        return null;
    }

    private static BigDecimal voltageFrom(final short value) {
        return valueOf(value).setScale(2).divide(valueOf(1000), HALF_UP); // TODO voltages to better be represented with scale 3
    }

    private static boolean locateGpsPower(final AvlIoElement io) {
        for (int index = 0; index < io.byteIo.length; index++) {
            if (io.byteIo[index].ioId == AvlIoCodes.GPS_POWER.id) {
                return io.byteIo[index].ioValue == 1;
            }
        }
        return false;
    }

    private static boolean locateIgnition(final AvlIoElement io) {
        for (int index = 0; index < io.byteIo.length; index++) {
            if (io.byteIo[index].ioId == AvlIoCodes.IGNITION.id) {
                return io.byteIo[index].ioValue == 1;
            }
        }
        return false;
    }

    private static Integer locateTotalOdometer(final AvlIoElement io) {
        for (int index = 0; index < io.intIo.length; index++) {
            if (io.intIo[index].ioId == AvlIoCodes.TOTAL_ODOMETER.id) {
                return io.intIo[index].ioValue;
            }
        }
        return null;
    }

    private static Integer locateTripOdometer(final AvlIoElement io) {
        for (int index = 0; index < io.intIo.length; index++) {
            if (io.intIo[index].ioId == AvlIoCodes.TRIP_ODOMETER.id) {
                return io.intIo[index].ioValue;
            }
        }
        return null;
    }

    private static boolean locateTrip(final AvlIoElement io) {
        for (int index = 0; index < io.byteIo.length; index++) {
            if (io.byteIo[index].ioId == AvlIoCodes.TRIP.id) {
                return io.byteIo[index].ioValue == 1;
            }
        }
        return false;
    }

}