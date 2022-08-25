package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.gis.gps.monitoring.impl.ITransporterMachineMonitoringProvider;
/** 
 * DAO implementation for companion object {@link ITgMachine}.
 * 
 * @author Developers
 *
 */
@EntityType(TgMachine.class)
public class TgMachineDao extends CommonEntityDao<TgMachine> implements ITgMachine {
    private final ITransporterMachineMonitoringProvider machineMonitoringProvider;

    @Inject
    public TgMachineDao(final IFilter filter, final ITransporterMachineMonitoringProvider machineMonitoringProvider) {
        super(filter);
        this.machineMonitoringProvider = machineMonitoringProvider;
    }

    @Override
    protected IFetchProvider<TgMachine> createFetchProvider() {
        // needed for autocompletion of 'this' property on corresponding centre
        return super.createFetchProvider().with("key", "desc");
    }

    @Override
    @SessionRequired
    public TgMachine save(final TgMachine entity) {
        final TgMachine result = super.save(entity);
        if (entity.isPersisted()) {
            // changed machine needs to be promoted to server cache to correctly handle machine processing
            this.machineMonitoringProvider.promoteChangedMachine(result);
        } else {
            // new machine needs to be promoted to server cache to correctly handle machine processing
            this.machineMonitoringProvider.promoteNewMachine(result);
        }
        return result;
    }

}