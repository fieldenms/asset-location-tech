package ua.com.fielden.platform.gis.gps;

/**
 * A contract for looking up a module by its IMEI.
 * 
 * @author TG Team
 * 
 */
public interface IModuleLookup {

    boolean isPresent(final String imei);

}