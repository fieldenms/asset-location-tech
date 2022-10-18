package fielden.teltonika;

/**
 * Teltonika specific IO element codes.
 * 
 * @author TG Team
 * 
 */
public enum AvlIoCodes {
    DIN1(1), EXTERNAL_VOLT(66), BATTERY_VOLT(67), GNSS_STATUS(69), IGNITION(239), TOTAL_ODOMETER(16), TRIP_ODOMETER(199), TRIP(250);

    public final int id;

    AvlIoCodes(final int val) {
        this.id = val;
    }
}
