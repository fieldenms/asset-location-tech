package ua.com.fielden.platform.gis.gps.monitoring.impl;

import ua.com.fielden.platform.gis.gps.monitoring.IMachineMonitoringProvider;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgModule;

/**
 * A contract to provide access to machine related monitoring information that gets updated asynchronously at runtime during receiving of GPS messages.
 *
 * @author TG Team
 *
 */
public interface ITransporterMachineMonitoringProvider extends IMachineMonitoringProvider<TgMessage, TgMachine, TgModule, TgMachineModuleAssociation> {
}