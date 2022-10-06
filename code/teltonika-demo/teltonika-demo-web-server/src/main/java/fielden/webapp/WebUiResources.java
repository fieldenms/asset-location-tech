package fielden.webapp;

import org.restlet.Context;
import org.restlet.routing.Router;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.application.AbstractWebUiResources;
import ua.com.fielden.platform.web.sse.resources.EventSourcingResourceFactory;
import ua.com.fielden.platform.web.test.eventsources.TgMessageEventSource;

/**
 * Custom {@link AbstractWebUiResources} for configuring domain specific web resources.
 *
 * @author Generated
 *
 */
public class WebUiResources extends AbstractWebUiResources {

    /**
     * Creates an instance of {@link WebUiResources} (for more information about the meaning of all this arguments see {@link AbstractWebUiResources#AbstractWebApp}
     *
     * @param context
     * @param injector
     * @param name
     * @param desc
     * @param owner
     * @param author
     * @param username
     */
    public WebUiResources(
            final Context context,
            final Injector injector,
            final String name,
            final String desc,
            final String owner,
            final String author,
            final IWebUiConfig webApp) {
        super(context, injector, name, desc, owner, author, webApp);
    }
    
    @Override
    protected void registerDomainWebResources(final Router router, final IWebUiConfig webApp) {
        // register custom resources with router.attach calls...
        router.attach("/sse/message-update-events", new EventSourcingResourceFactory(injector, TgMessageEventSource.class, deviceProvider, dates));
    }

}
