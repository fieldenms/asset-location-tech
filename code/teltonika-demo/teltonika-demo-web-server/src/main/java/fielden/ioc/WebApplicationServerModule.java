package fielden.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.binder.AnnotatedBindingBuilder;

import fielden.webapp.WebUiConfig;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.gis.gps.actors.impl.TransporterMachineMonitoringProvider;
import ua.com.fielden.platform.gis.gps.monitoring.impl.ITransporterMachineMonitoringProvider;
import ua.com.fielden.platform.reflection.CompanionObjectAutobinder;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgModule;
import ua.com.fielden.platform.security.ServerAuthorisationModel;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.utils.DefaultDates;
import ua.com.fielden.platform.utils.DefaultUniversalConstants;
import ua.com.fielden.platform.web.ioc.IBasicWebApplicationServerModule;

/**
 * Guice injector module for Teltonika Demo (Legacy) server.
 *
 * @author TG Team
 *
 */
public class WebApplicationServerModule extends ApplicationServerModule implements IBasicWebApplicationServerModule {

    private final String domainName;
    private final String path;
    private final int port;
    private final Workflows workflow;

    public WebApplicationServerModule(
            final Map<Class, Class> defaultHibernateTypes,
            final IApplicationDomainProvider applicationDomainProvider,
            final List<Class<? extends AbstractEntity<?>>> domainTypes,
            final Class<? extends ISerialisationClassProvider> serialisationClassProviderType,
            final Class<? extends IFilter> automaticDataFilterType,
            final Properties props) throws Exception {
        super(defaultHibernateTypes, applicationDomainProvider, domainTypes, serialisationClassProviderType, automaticDataFilterType, ServerAuthorisationModel.class, DefaultUniversalConstants.class, DefaultDates.class, props);
        this.domainName = props.getProperty("web.domain");
        this.port = Integer.valueOf(props.getProperty("port"));
        this.path = props.getProperty("web.path");

        this.workflow = Workflows.valueOf(props.getProperty("workflow"));
    }

    @Override
    protected void configure() {
        super.configure();
        bindWebAppResources(new WebUiConfig(domainName, port, workflow, path));
        
        // bind machine monitor provider
        bindType(ITransporterMachineMonitoringProvider.class).to(TransporterMachineMonitoringProvider.class).in(Scopes.SINGLETON);
        
        //bindType(ITgMachine.class).to(TgMachineDao.class);
        CompanionObjectAutobinder.bindCo(TgMachine.class, binder());
        CompanionObjectAutobinder.bindCo(TgModule.class, binder());
        CompanionObjectAutobinder.bindCo(TgMachineModuleAssociation.class, binder());
    }

    @Override
    public void setInjector(final Injector injector) {
        super.setInjector(injector);
        initWebApp(injector);
    }

    @Override
    public <T> AnnotatedBindingBuilder<T> bindType(final Class<T> clazz) {
        return bind(clazz);
    }
}
