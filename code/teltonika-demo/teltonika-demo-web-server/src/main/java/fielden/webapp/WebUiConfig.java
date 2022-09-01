package fielden.webapp;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import org.apache.commons.lang3.StringUtils;

import fielden.config.Modules;
import fielden.config.personnel.PersonWebUiConfig;
import fielden.personnel.Person;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.ui.menu.sample.MiTgMachineRealtimeMonitor;
import ua.com.fielden.platform.ui.menu.sample.MiTgMessage;
import ua.com.fielden.platform.ui.menu.sample.MiTgPolygon;
import ua.com.fielden.platform.ui.menu.sample.MiTgStop;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.resources.webui.AbstractWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.SecurityMatrixWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.UserRoleWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.UserWebUiConfig;
import ua.com.fielden.platform.web.test.server.TgMachineRealtimeMonitorWebUiConfig;
import ua.com.fielden.platform.web.test.server.TgMessageWebUiConfig;
import ua.com.fielden.platform.web.test.server.TgPolygonWebUiConfig;
import ua.com.fielden.platform.web.test.server.TgStopWebUiConfig;

/**
 * App-specific {@link IWebApp} implementation.
 *
 * @author Generated
 *
 */
public class WebUiConfig extends AbstractWebUiConfig {

    public static final String WEB_TIME_WITH_MILLIS_FORMAT = "HH:mm:ss.SSS";
    public static final String WEB_TIME_FORMAT = "HH:mm";
    public static final String WEB_DATE_FORMAT_JS = "DD/MM/YYYY";
    public static final String WEB_DATE_FORMAT_JAVA = fromJsToJavaDateFormat(WEB_DATE_FORMAT_JS);

    private final String domainName;
    private final String path;
    private final int port;

    public WebUiConfig(final String domainName, final int port, final Workflows workflow, final String path) {
        super("Teltonika Demo (Legacy)", workflow, new String[] { "fielden/" });
        if (StringUtils.isEmpty(domainName) || StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("Both the domain name and application binding path should be specified.");
        }
        this.domainName = domainName;
        this.port = port;
        this.path = path;
    }


    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public int getPort() {
        return port;
    }

    /**
     * Configures the {@link WebUiConfig} with custom centres and masters.
     */
    @Override
    public void initConfiguration() {
        super.initConfiguration();

        final IWebUiBuilder builder = configApp();
        builder.setDateFormat(WEB_DATE_FORMAT_JS).setTimeFormat(WEB_TIME_FORMAT).setTimeWithMillisFormat(WEB_TIME_WITH_MILLIS_FORMAT)
        .setMinTabletWidth(600);

        // Users and Personnel Module
        final PersonWebUiConfig personWebUiConfig = PersonWebUiConfig.register(injector(), builder);
        final UserWebUiConfig userWebUiConfig = UserWebUiConfig.register(injector(), builder);
        final UserRoleWebUiConfig userRoleWebUiConfig = UserRoleWebUiConfig.register(injector(), builder);
        final SecurityMatrixWebUiConfig securityConfig = SecurityMatrixWebUiConfig.register(injector(), configApp());

        // Add user-rated masters and centres to the configuration 
        configApp()
        .addMaster(userWebUiConfig.master)
        .addMaster(userWebUiConfig.rolesUpdater)
        .addMaster(userRoleWebUiConfig.master)
        .addMaster(userRoleWebUiConfig.tokensUpdater)
        .addCentre(userWebUiConfig.centre)
        .addCentre(userRoleWebUiConfig.centre);

        TgMessageWebUiConfig.register(injector(), configApp());
        TgStopWebUiConfig.register(injector(), configApp());
        TgMachineRealtimeMonitorWebUiConfig.register(injector(), configApp());
        TgPolygonWebUiConfig.register(injector(), configApp());

        // Configure application menu
        configDesktopMainMenu()
        .addModule(Modules.USERS_AND_PERSONNEL.title)
            .description(Modules.USERS_AND_PERSONNEL.desc)
            .icon(Modules.USERS_AND_PERSONNEL.icon)
            .detailIcon(Modules.USERS_AND_PERSONNEL.icon)
            .bgColor(Modules.USERS_AND_PERSONNEL.bgColour)
            .captionBgColor(Modules.USERS_AND_PERSONNEL.captionBgColour)
            .menu()
                .addMenuItem(mkMenuItemTitle(Person.class)).description(mkMenuItemDesc(Person.class)).centre(personWebUiConfig.centre).done()
                .addMenuItem("System Users").description("Functionality for managing system users, athorisation, etc.")
                    .addMenuItem("Users").description("User centre").centre(userWebUiConfig.centre).done()
                    .addMenuItem("User Roles").description("User roles centre").centre(userRoleWebUiConfig.centre).done()
                    .addMenuItem("Security Matrix").description("Security Matrix is used to manage application authorisations for User Roles.").master(securityConfig.master).done()
                .done()
                .addMenuItem("GPS-tracks").description(
                        "Перегляд, моніторинг та аналіз GPS повідомлень (у вигляді треків), отриманих від GPS-модулів, які встановлені на машини компанії." + //
                        "Є можливість переглядати обчислений кілометраж у вигляді графіка і / або таблиці."
                ).icon("icons:cloud-queue").centre(configApp().getCentre(MiTgMessage.class).get()).done()
                .addMenuItem("Зупинки").description(
                        "Перегляд, моніторинг та аналіз зупинок, які були здійснені машинами компанії." + "<br><br>"
                      + "Зупинка означає, що машина деякий час простоювала або повільно їхала в межах певної невеликої території. Порогові значення "
                      + "для радіусу території чи швидкості переміщення задає користувач. Також можна задавати "
                      + "пошук по машинах, організаційних підрозділах та часу здійснення зупинки."
                ).icon("icons:card-giftcard").centre(configApp().getCentre(MiTgStop.class).get()).done()
                .addMenuItem("Моніторинг в реальному часі").description(
                        "Центр для перегляду машин у реальному часі на карті."
                ).icon("icons:open-with").centre(configApp().getCentre(MiTgMachineRealtimeMonitor.class).get()).done()
                .addMenuItem("Гео-зони").description(
                        "Перегляд, моніторинг та аналіз гео-зон."
                ).icon("icons:flag").centre(configApp().getCentre(MiTgPolygon.class).get()).done()
            .done().done()
        .setLayoutFor(Device.DESKTOP, null, "[[[]]]")
        .setLayoutFor(Device.TABLET, null, "[[[]]]")
        .setLayoutFor(Device.MOBILE, null, "[[[]]]")
        .minCellWidth(100).minCellHeight(148).done();
    }

    private static String fromJsToJavaDateFormat(final String dateFormatJs) {
        return dateFormatJs.replace("DD", "dd").replace("YYYY", "yyyy"); // UPPERCASE "Y" is "week year" in Java, therefore we prefer lowercase "y"
    }

    public static String mkMenuItemTitle(final Class<? extends AbstractEntity<?>> entityType) {
        return getEntityTitleAndDesc(entityType).getKey();
    }

    public static final String CENTRE_SUFFIX = " Centre";
    public static String mkMenuItemDesc(final Class<? extends AbstractEntity<?>> entityType) {
        final Pair<String, String> titleDesc = TitlesDescsGetter.getEntityTitleAndDesc(entityType);
        // Some @EntityTitle desc are not specified, while the others are worded as whole sentence ending with "." - use value in both cases
        return titleDesc.getValue().isEmpty() || titleDesc.getValue().endsWith(".") ? titleDesc.getKey() + CENTRE_SUFFIX : titleDesc.getValue() + CENTRE_SUFFIX;
    }

}