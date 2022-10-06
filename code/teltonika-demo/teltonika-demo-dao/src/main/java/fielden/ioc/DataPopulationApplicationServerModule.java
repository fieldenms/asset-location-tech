package fielden.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.inject.Scopes;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.gis.gps.monitoring.impl.ITransporterMachineMonitoringProvider;
import ua.com.fielden.platform.reflection.CompanionObjectAutobinder;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgModule;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

public class DataPopulationApplicationServerModule extends ApplicationServerModule {

    public DataPopulationApplicationServerModule(final Map<Class, Class> defaultHibernateTypes, final IApplicationDomainProvider applicationDomainProvider, final List<Class<? extends AbstractEntity<?>>> domainTypes, final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, final Class<? extends IFilter> automaticDataFilterType, final Class<? extends IAuthorisationModel> authorisationModelType, final Class<? extends IUniversalConstants> universalConstantsType, final Class<? extends IDates> datesImplType, final Properties props)
            throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, domainTypes, serialisationClassProviderType, automaticDataFilterType, authorisationModelType, universalConstantsType, datesImplType, props);
    }

    @Override
    protected void configure() {
        super.configure();
        
        // bind machine monitor provider
        bind(ITransporterMachineMonitoringProvider.class).to(MockTransporterMachineMonitoringProvider.class).in(Scopes.SINGLETON);
        
        CompanionObjectAutobinder.bindCo(TgMachine.class, binder());
        CompanionObjectAutobinder.bindCo(TgModule.class, binder());
        CompanionObjectAutobinder.bindCo(TgMachineModuleAssociation.class, binder());
    }

}
