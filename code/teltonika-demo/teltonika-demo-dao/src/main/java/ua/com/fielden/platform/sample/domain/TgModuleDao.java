package ua.com.fielden.platform.sample.domain;

import java.util.Map;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.gis.gps.monitoring.impl.ITransporterMachineMonitoringProvider;

/**
 * DAO implementation for companion object {@link ITgModule}.
 *
 * @author Developers
 *
 */
@EntityType(TgModule.class)
public class TgModuleDao extends CommonEntityDao<TgModule> implements ITgModule {
    private final ITransporterMachineMonitoringProvider machineMonitoringProvider;

    @Inject
    public TgModuleDao(final IFilter filter, final ITransporterMachineMonitoringProvider machineMonitoringProvider) {
        super(filter);
        this.machineMonitoringProvider = machineMonitoringProvider;
    }

    @Override
    @SessionRequired
    public void delete(final TgModule entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgModule> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }

    @Override
    @SessionRequired
    public TgModule save(final TgModule entity) {
        final TgModule result = super.save(entity);
        if (entity.isPersisted()) {
            // changed module needs to be promoted to server cache to correctly handle module processing
            this.machineMonitoringProvider.promoteChangedModule(result);
            return result;
        } else {
            // new module needs to be promoted to server cache to correctly handle module processing
            this.machineMonitoringProvider.promoteNewModule(result);
        }
        return result;
    }

}