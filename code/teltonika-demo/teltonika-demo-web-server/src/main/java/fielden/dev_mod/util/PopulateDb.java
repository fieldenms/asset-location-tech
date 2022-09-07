package fielden.dev_mod.util;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.joda.time.DateTime;

import fielden.config.ApplicationDomain;
import fielden.personnel.Person;
import ua.com.fielden.platform.devdb_support.DomainDrivenDataPopulation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.sample.domain.ITgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgJourneyOverNightStay;
import ua.com.fielden.platform.sample.domain.TgJourneyPurpose;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineDriverAssociation;
import ua.com.fielden.platform.sample.domain.TgMachineDriverAssociationCo;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgModule;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.IDomainDrivenTestCaseConfiguration;
import ua.com.fielden.platform.utils.DbUtils;

/**
 * This is a convenience class for (re-)creation of the development database and its population.
 * 
 * It contains the <code>main</code> method and can be executed whenever the target database needs to be (re-)set.
 * <p>
 * 
 * <b>IMPORTANT: </b><i>One should be careful not to run this code against the deployment or production databases, which would lead to the loss of all data.</i>
 * 
 * <p>
 * 
 * @author Generated
 * 
 */
public class PopulateDb extends DomainDrivenDataPopulation {
	private static final Logger LOGGER = getLogger(PopulateDb.class);

    private final ApplicationDomain applicationDomainProvider = new ApplicationDomain();

    private PopulateDb(final IDomainDrivenTestCaseConfiguration config, final Properties props) {
        super(config, props);
    }

    public static void main(final String[] args) throws Exception {
        LOGGER.info("Initialising...");
        final String configFileName = args.length == 1 ? args[0] : "application.properties";
        final Properties props = new Properties();
        try (final FileInputStream in = new FileInputStream(configFileName)) {
            props.load(in);
        }

        LOGGER.info("Obtaining Hibernate dialect...");
        final Class<?> dialectType = Class.forName(props.getProperty("hibernate.dialect"));
        final Dialect dialect = (Dialect) dialectType.newInstance();
        LOGGER.info(format("Running with dialect %s...", dialect));
        final DataPopulationConfig config = new DataPopulationConfig(props);
        LOGGER.info("Generating DDL and running it against the target DB...");

        // use TG DDL generation or
        // Hibernate DDL generation final List<String> createDdl = DbUtils.generateSchemaByHibernate()
        final List<String> createDdl = config.getDomainMetadata().generateDatabaseDdl(dialect); // , TgMessage.class); createDdl.stream().forEach(System.out::println); // GenDdl-like; comment following lines
        final List<String> ddl = dialect instanceof H2Dialect ?
                                 DbUtils.prependDropDdlForH2(createDdl) :
                                 DbUtils.prependDropDdlForSqlServer(createDdl);
        DbUtils.execSql(ddl, config.getInstance(HibernateUtil.class).getSessionFactory().getCurrentSession());

        final PopulateDb popDb = new PopulateDb(config, props);
        popDb.populateDomain();
    }

    @Override
    protected void populateDomain() {
        LOGGER.info("Creating and populating the development database...");
        
//        JourneyProcessor.createJourneysFrom(
//            getInstance(ITgMessage.class).getAllEntities(from(select(TgMessage.class)
//                    .where().prop("gpsTime").gt().val(new DateTime(2022, 9, 2, 0, 0).toDate())
//                    .model()).with(fetchAll(TgMessage.class)).model()),
//            getInstance(ITgMachine.class).getEntity(from(select(TgMachine.class).model()).with(fetchAll(TgMachine.class)).model()),
//            getInstance(TgJourneyCo.class),
//            getInstance(ITgMachineModuleAssociation.class),
//            getInstance(TgMachineDriverAssociationCo.class)
//        );
        setupUser(User.system_users.SU, "fielden");
        final var person = setupPerson(User.system_users.SU, "fielden");

        save(new_(TgJourneyPurpose.class, "BM").setDesc("BM journey purpose."));
        save(new_(TgJourneyPurpose.class, "TR").setDesc("TR journey purpose."));

        save(new_(TgJourneyOverNightStay.class, "HM").setDesc("Home."));
        save(new_(TgJourneyOverNightStay.class, "WK").setDesc("Work."));
        save(new_(TgJourneyOverNightStay.class, "GR").setDesc("Garage."));

        LOGGER.info("\tPopulating testing machine + module...");
        final TgMachine machine00 = save(new_(TgMachine.class, "00000001").setDesc("Machine for testing with 867648048071573 module."));
        final TgModule module00 = save(new_(TgModule.class, "867648048071573")
            .setSerialNo(999)
            .setGpsFirmware("1.0")
            .setHwVersion("1.0")
            .setImletVersion("1.0")
            .setIdentifier("id")
            .setDesc("Module 867648048071573 for testing."));
        final TgMachineModuleAssociation assoc00 = new_composite(TgMachineModuleAssociation.class, machine00, module00, new DateTime().withTimeAtStartOfDay().toDate()).setInitOdometer(158292);
        getInstance(ITgMachineModuleAssociation.class).regularSave(assoc00);
        getInstance(TgMachineDriverAssociationCo.class).regularSave(new_composite(TgMachineDriverAssociation.class, machine00, person, new DateTime().withTimeAtStartOfDay().toDate()));
        //save(assoc00);
        final TgMachine machine01 = save(new_(TgMachine.class, "00000002").setDesc("A vehicle for testing with 860264054087367 module."));
        final TgModule module01 = save(new_(TgModule.class, "860264054087367")
            .setSerialNo(999)
            .setGpsFirmware("1.0")
            .setHwVersion("1.0")
            .setImletVersion("1.0")
            .setIdentifier("id")
            .setDesc("FMC001 860264054087367 for testing."));
        final TgMachineModuleAssociation assoc01 = new_composite(TgMachineModuleAssociation.class, machine01, module01, new DateTime().withTimeAtStartOfDay().toDate()).setInitOdometer(99999);
        getInstance(ITgMachineModuleAssociation.class).regularSave(assoc01);
        getInstance(TgMachineDriverAssociationCo.class).regularSave(new_composite(TgMachineDriverAssociation.class, machine01, person, new DateTime().withTimeAtStartOfDay().toDate()));

        
//        LOGGER.info("\tPopulating messages...");
//        final Map<String, TgMachine> machines = new HashMap<>();
//        try {
//            final ClassLoader classLoader = getClass().getClassLoader();
//            final File file = new File(classLoader.getResource("gis/messageEntities.js").getFile());
//            final InputStream stream = new FileInputStream(file);
//            final ObjectMapper objectMapper = new ObjectMapper();
//            final ArrayList oldMessageEntities = objectMapper.readValue(stream, ArrayList.class);
//
//            for (final Object oldMessageEntity: oldMessageEntities) {
//                final Map<String, Object> map = (Map<String, Object>) oldMessageEntity;
//                final Map<String, Object> messageProps = ((Map<String, Object>) map.get("properties"));
//                final String machineKey = (String) ((Map<String, Object>) ((Map<String, Object>) messageProps.get("machine")).get("properties")).get("key");
//                TgMachine found = machines.get(machineKey);
//                if (found == null) {
//                    final TgMachine newMachine = new_(TgMachine.class, machineKey);
//                    newMachine.setDesc(machineKey + " desc");
//                    found = save(newMachine);
//                    machines.put(machineKey, found);
//                }
//                save(new_composite(TgMessage.class, found, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").parseDateTime(((String) messageProps.get("gpsTime"))).toDate())
//                        .setX(BigDecimal.valueOf((double) messageProps.get("x")))
//                        .setY(BigDecimal.valueOf((double) messageProps.get("y")))
//                        .setVectorAngle((int) messageProps.get("vectorAngle"))
//                        .setVectorSpeed((int) messageProps.get("vectorSpeed"))
//                        // .setAltitude(223)
//                        // .setVisibleSattelites(2)
//                        .setDin1((boolean) messageProps.get("din1"))
//                        .setGpsPower((boolean) messageProps.get("gpsPower"))
//                        .setTravelledDistance(BigDecimal.valueOf((double) messageProps.get("travelledDistance")))
//                );
//            }
//        } catch (final IOException ex) {
//            throw new IllegalStateException(ex);
//        }
//
//        LOGGER.info("\tPopulating machines...");
//        try {
//            final ClassLoader classLoader = getClass().getClassLoader();
//            final File file = new File(classLoader.getResource("gis/realtimeMonitorEntities.js").getFile());
//            final InputStream stream = new FileInputStream(file);
//            final ObjectMapper objectMapper = new ObjectMapper();
//            final ArrayList oldMachineEntities = objectMapper.readValue(stream, ArrayList.class);
//
//            final Map<String, TgOrgUnit> orgUnits = new HashMap<>();
//            for (final Object oldMachineEntity: oldMachineEntities) {
//                final Map<String, Object> map = (Map<String, Object>) oldMachineEntity;
//                final Map<String, Object> machineProps = ((Map<String, Object>) map.get("properties"));
//                final String machineKey = (String) machineProps.get("key");
//                TgMachine found = machines.get(machineKey);
//                if (found == null) {
//                    final TgMachine newMachine = new_(TgMachine.class, machineKey);
//                    newMachine.setDesc((String) machineProps.get("desc"));
//                    final Object orgUnitObject = machineProps.get("orgUnit");
//                    if (orgUnitObject != null) {
//                        final String orgUnitKey = (String) ((Map<String, Object>) ((Map<String, Object>) orgUnitObject).get("properties")).get("key");
//                        TgOrgUnit foundOrgUnit = orgUnits.get(orgUnitKey);
//                        if (foundOrgUnit == null) {
//                            final TgOrgUnit newOrgUnit = new_(TgOrgUnit.class, orgUnitKey);
//                            newOrgUnit.setDesc((String) ((Map<String, Object>) ((Map<String, Object>) machineProps.get("orgUnit")).get("properties")).get("desc"));
//                            foundOrgUnit = save(newOrgUnit);
//                            orgUnits.put(orgUnitKey, foundOrgUnit);
//                        }
//                        newMachine.setOrgUnit(foundOrgUnit);
//                    }
//                    found = save(newMachine);
//                    machines.put(machineKey, found);
//                }
//                final Object lastMessageObject = machineProps.get("lastMessage");
//                if (lastMessageObject != null) {
//                    final Map<String, Object> lastMessageProps = (Map<String, Object>) ((Map<String, Object>) lastMessageObject).get("properties");
//
//                    save(new_composite(TgMessage.class, found, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").parseDateTime(((String) lastMessageProps.get("gpsTime"))).toDate())
//                            .setX(BigDecimal.valueOf((double) lastMessageProps.get("x")))
//                            .setY(BigDecimal.valueOf((double) lastMessageProps.get("y")))
//                            .setVectorAngle((int) lastMessageProps.get("vectorAngle"))
//                            .setVectorSpeed((int) lastMessageProps.get("vectorSpeed"))
//                            // .setAltitude(223)
//                            // .setVisibleSattelites(2)
//                            .setDin1((boolean) lastMessageProps.get("din1"))
//                            .setGpsPower((boolean) lastMessageProps.get("gpsPower"))
//                            .setTravelledDistance(BigDecimal.valueOf(15.5)) // lastMessageProps.get("travelledDistance")
//                    );
//                }
//            }
//        } catch (final IOException ex) {
//            throw new IllegalStateException(ex);
//        }
//
//        LOGGER.info("\tPopulating geozones...");
//        try {
//            final ClassLoader classLoader = getClass().getClassLoader();
//            final File file = new File(classLoader.getResource("gis/polygonEntities.js").getFile());
//            final InputStream stream = new FileInputStream(file);
//            final ObjectMapper objectMapper = new ObjectMapper();
//            final ArrayList oldPolygonEntities = objectMapper.readValue(stream, ArrayList.class);
//
//            final Map<String, TgPolygon> polygons = new HashMap<>();
//            for (final Object oldPolygonEntity: oldPolygonEntities) {
//                final Map<String, Object> map = (Map<String, Object>) oldPolygonEntity;
//                final Map<String, Object> polygonProps = ((Map<String, Object>) map.get("properties"));
//                final String polygonKey = (String) polygonProps.get("key");
//                TgPolygon found = polygons.get(polygonKey);
//                if (found == null) {
//                    final TgPolygon newPolygon = new_(TgPolygon.class, polygonKey);
//                    newPolygon.setDesc((String) polygonProps.get("desc"));
//                    found = save(newPolygon);
//                    polygons.put(polygonKey, found);
//                }
//
//                final ArrayList<Object> coordinates = (ArrayList<Object>) polygonProps.get("coordinates");
//                for (final Object coord: coordinates) {
//                    final Map<String, Object> coordProps = ((Map<String, Object>) ((Map<String, Object>) coord).get("properties"));
//                    save(new_composite(TgCoordinate.class, found, (Integer) coordProps.get("order"))
//                            .setLongitude(BigDecimal.valueOf((double) coordProps.get("longitude")))
//                            .setLatitude(BigDecimal.valueOf((double) coordProps.get("latitude")))
//                    );
//                }
//            }
//        } catch (final IOException ex) {
//            throw new IllegalStateException(ex);
//        }

        LOGGER.info("Completed database creation and population.");
	}

    private Person setupPerson(final User.system_users defaultUser, final String emailDomain) {
        final User su = co(User.class).findByKey(defaultUser.name());
        return save(new_(Person.class).setEmail(defaultUser + "@" + emailDomain).setDesc("Super Person").setUser(su));
    }

    @Override
    protected List<Class<? extends AbstractEntity<?>>> domainEntityTypes() {
        return applicationDomainProvider.entityTypes();
    }

}
