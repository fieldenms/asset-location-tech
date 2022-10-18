package fielden.teltonika;

/**
 * A contract for AVL tracker authorisation and data handling by its IMEI.
 * 
 * @author TG Team
 * 
 */
public interface IAvlTrackerHandler {

    /**
     * Allows or disallows further data handling for AVL tracker with {@code imei}.
     * 
     * @param imei
     */
    boolean authorise(final String imei);

    /**
     * Handles newly received {@code data} for AVL tracker with {@code imei}.
     * 
     * @param imei
     * @param data
     */
    void handleData(final String imei, final AvlData[] data);

}