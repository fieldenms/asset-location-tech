package fielden.test_config;

import java.util.Properties;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IdOnlyProxiedEntityTypeCache;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserNotifierMockBindingModule;
import ua.com.fielden.platform.security.NoAuthorisation;
import ua.com.fielden.platform.test.DbDrivenTestCase;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.test.ioc.DatesForTesting;
import ua.com.fielden.platform.test.ioc.UniversalConstantsForTesting;

import com.google.inject.Injector;

import fielden.config.ApplicationDomain;
import fielden.dbsetup.HibernateSetup;
import fielden.filter.NoDataFilter;
import fielden.ioc.ApplicationServerModule;
import fielden.serialisation.SerialisationClassProvider;

/**
 * Provides implementation of {@link IDomainDrivenTestCaseConfiguration} for testing purposes, which is mainly related to construction of appropriate IoC modules.
 *
 * @author Generated
 *
 */
public final class DaoDomainDrivenTestCaseConfiguration implements IDomainDrivenTestCaseConfiguration {
    private final EntityFactory entityFactory;
    private final Injector injector;
    private final ApplicationServerModule iocModule;

    /**
     * Required for dynamic instantiation by {@link DbDrivenTestCase}
     */
    public DaoDomainDrivenTestCaseConfiguration(final Properties props) {
    	try {
    	    // application properties
    	    props.setProperty("app.name", "Teltonika Demo (Legacy)");
    	    props.setProperty("email.smtp", "non-existing-server");
    	    props.setProperty("email.fromAddress", "support@teltonika-demo.com.au");
    	    props.setProperty("reports.path", "");
    	    props.setProperty("domain.path", "../teltonika-demo-pojo-bl/target/classes");
    	    props.setProperty("domain.package", "fielden");
    	    props.setProperty("tokens.path", "../teltonika-demo-pojo-bl/target/classes");
    	    props.setProperty("tokens.package", "fielden.security.tokens");
            props.setProperty("attachments.location", "../teltonika-demo-web-server/src/test/resources/attachments");
    	    props.setProperty("workflow", "development");
    	    // custom Hibernate configuration properties
    	    props.setProperty("hibernate.show_sql", "false");
    	    props.setProperty("hibernate.format_sql", "true");
    	    props.setProperty("cacheDefaults", "false");
    	    
    	    final ApplicationDomain applicationDomainProvider = new ApplicationDomain();
    	    
            iocModule = new ApplicationServerModule(
                    HibernateSetup.getHibernateTypes(),
                    applicationDomainProvider,
                    applicationDomainProvider.domainTypes(),
                    SerialisationClassProvider.class,
                    NoDataFilter.class,
                    NoAuthorisation.class,
                    UniversalConstantsForTesting.class,
                    DatesForTesting.class,
                    props);
    
    	    injector = new ApplicationInjectorFactory()
                    .add(iocModule)
                    .add(new NewUserNotifierMockBindingModule())
                    .getInjector();
    
            entityFactory = injector.getInstance(EntityFactory.class);
    	} catch (final Exception e) {
    	    e.printStackTrace();
    	    throw new RuntimeException(e);
    	}
    }

    @Override
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

    @Override
    public DomainMetadata getDomainMetadata() {
        return iocModule.getDomainMetadata();
    }

    @Override
    public IdOnlyProxiedEntityTypeCache getIdOnlyProxiedEntityTypeCache() {
        return iocModule.getIdOnlyProxiedEntityTypeCache();
    }
}
