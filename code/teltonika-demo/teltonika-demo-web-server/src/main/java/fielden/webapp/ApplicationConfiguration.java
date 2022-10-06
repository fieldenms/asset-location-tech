package fielden.webapp;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.restlet.Component;

import com.google.inject.Injector;

import fielden.config.ApplicationDomain;
import fielden.dbsetup.HibernateSetup;
import fielden.filter.NoDataFilter;
import fielden.ioc.WebApplicationServerModule;
import fielden.serialisation.SerialisationClassProvider;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.impl.Actors;
import ua.com.fielden.platform.gis.gps.actors.impl.MachineActor;
import ua.com.fielden.platform.gis.gps.actors.impl.ModuleActor;
import ua.com.fielden.platform.gis.gps.actors.impl.ViolatingMessageResolverActor;
import ua.com.fielden.platform.gis.gps.factory.impl.AbstractTransporterApplicationConfigurationUtil;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierBindingModule;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgModule;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.factories.webui.LoginCompleteResetResourceFactory;
import ua.com.fielden.platform.web.factories.webui.LoginInitiateResetResourceFactory;
import ua.com.fielden.platform.web.factories.webui.LoginResourceFactory;
import ua.com.fielden.platform.web.factories.webui.LogoutResourceFactory;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.LoginCompleteResetResource;
import ua.com.fielden.platform.web.resources.webui.LoginInitiateResetResource;
import ua.com.fielden.platform.web.resources.webui.LoginResource;
import ua.com.fielden.platform.web.resources.webui.LogoutResource;

/**
 * Configuration point for Teltonika Demo (Legacy) Web Application.
 * 
 * @author Generated
 * 
 */
public class ApplicationConfiguration extends Component {

    private final Injector injector;

    public ApplicationConfiguration(final Properties props) {
        // /////////////////////////////////////////////////////
        // ////// configure Hibernate and Guice injector ///////
        // /////////////////////////////////////////////////////
        try {
            // create application IoC module and injector
            final ApplicationDomain applicationDomainProvider = new ApplicationDomain();

            final WebApplicationServerModule module = new WebApplicationServerModule(
                    HibernateSetup.getHibernateTypes(),
                    applicationDomainProvider,
                    applicationDomainProvider.domainTypes(),
                    SerialisationClassProvider.class,
                    NoDataFilter.class,
                    props);
            injector = new ApplicationInjectorFactory()
                    .add(module)
                    .add(new NewUserEmailNotifierBindingModule())
                    .getInjector();

            ////////////////////////////////////////////////////////////////
            /////// Create a component with an HTTP server connector ///////
            ////////////////////////////////////////////////////////////////
            // application configuration 
            final IWebUiConfig webApp = injector.getInstance(IWebUiConfig.class);
            
            // attach system resources, which should be beyond the version scope
            // the interactive login page resource is considered one of the system resources, which does not require guarding
            getDefaultHost().attach(LoginResource.BINDING_PATH, new LoginResourceFactory(true, injector.getInstance(RestServerUtil.class), injector));
            getDefaultHost().attach(LoginInitiateResetResource.BINDING_PATH, new LoginInitiateResetResourceFactory(injector));
            getDefaultHost().attach(LoginCompleteResetResource.BINDING_PATH, new LoginCompleteResetResourceFactory(injector, "Imagination is the limit."));
            getDefaultHost().attach(LogoutResource.BINDING_PATH, new LogoutResourceFactory(webApp.getDomainName(), webApp.getPath(), injector));
            // attach a web resource that represents this application
            getDefaultHost().attach(
                    new WebUiResources(
                            getContext().createChildContext(),
                            injector,
                            "Teltonika Demo (Legacy)",
                            "An application server for Teltonika Demo (Legacy)",
                            "Fielden Management Services Pty. Ltd.",
                            "Authors",
                            webApp
                            ));

            final boolean emergencyMode = props.getProperty("mode").equalsIgnoreCase("emergency");
            final Integer windowSize = Integer.valueOf(props.getProperty("windowSize"));
            final Integer windowSize2 = Integer.valueOf(props.getProperty("windowSize2"));
            final Integer windowSize3 = Integer.valueOf(props.getProperty("windowSize3"));
            final Double thresh = Double.valueOf(props.getProperty("averagePacketSizeThreshould"));
            final Double thresh2 = Double.valueOf(props.getProperty("averagePacketSizeThreshould2"));

            new AbstractTransporterApplicationConfigurationUtil() {
                @Override
                protected AbstractActors<TgMessage, TgMachine, TgModule, TgMachineModuleAssociation, MachineActor, ModuleActor, ViolatingMessageResolverActor> createActors(final Map<TgMachine, TgMessage> machinesWithLastMessages, final Map<TgModule, List<TgMachineModuleAssociation>> modulesWithAssociations) {
                    return new Actors(injector, machinesWithLastMessages, modulesWithAssociations, props.getProperty("gps.host"), Integer.valueOf(props.getProperty("gps.port")), emergencyMode, windowSize, windowSize2, windowSize3, thresh, thresh2);
                }
            }
            .startGpsServices(props, injector);

        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Injector injector() {
        return injector;
    }
}
