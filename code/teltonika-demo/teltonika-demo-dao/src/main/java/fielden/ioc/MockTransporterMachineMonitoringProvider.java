package fielden.ioc;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.gis.gps.MachineServerState;
import ua.com.fielden.platform.gis.gps.monitoring.impl.ITransporterMachineMonitoringProvider;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgModule;

public class MockTransporterMachineMonitoringProvider implements ITransporterMachineMonitoringProvider {

    @Override
    public Map<Long, MachineServerState> getServerStatesUpdate(Map<Long, MachineServerState> serverStatesRequest) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Long, List<TgMessage>> getLastMessagesUpdate(Map<Long, Date> machinesTiming) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void promoteNewMachineAssociation(TgMachineModuleAssociation machineModuleTemporalAssociation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void promoteChangedMachineAssociation(TgMachineModuleAssociation machineModuleTemporalAssociation) {
        // TODO Auto-generated method stub

    }

    @Override
    public void promoteNewMachine(TgMachine machine) {
        // TODO Auto-generated method stub

    }

    @Override
    public void promoteNewModule(TgModule module) {
        // TODO Auto-generated method stub

    }

    @Override
    public void promoteChangedMachine(TgMachine machine) {
        // TODO Auto-generated method stub

    }

    @Override
    public void promoteChangedModule(TgModule module) {
        // TODO Auto-generated method stub

    }

}
