package ua.com.fielden.platform.gis.gps.actors.impl;

import ua.com.fielden.platform.gis.gps.monitoring.DefaultMachineMonitoringProvider;
import ua.com.fielden.platform.gis.gps.monitoring.impl.ITransporterMachineMonitoringProvider;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgModule;

/**
 * A default "carrier" implementation for {@link ITransporterMachineMonitoringProvider}.
 *
 * @author TG Team
 *
 */
public class TransporterMachineMonitoringProvider extends DefaultMachineMonitoringProvider<TgMessage, TgMachine, TgModule, TgMachineModuleAssociation> implements ITransporterMachineMonitoringProvider {

    @Override
    public Actors getActors() {
        return (Actors) super.getActors();
    }
}
