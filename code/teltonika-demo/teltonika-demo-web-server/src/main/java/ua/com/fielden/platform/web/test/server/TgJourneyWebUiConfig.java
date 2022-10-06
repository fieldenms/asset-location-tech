package ua.com.fielden.platform.web.test.server;

import static fielden.common.StandardScrollingConfigs.standardStandaloneScrollingConfig;
import static java.lang.String.format;
import static ua.com.fielden.platform.web.PrefDim.mkDim;

import java.util.Optional;

import com.google.inject.Injector;

import fielden.common.LayoutComposer;
import fielden.common.StandardActions;
import fielden.main.menu.personnel.MiTgJourney;
import fielden.personnel.Person;
import ua.com.fielden.platform.sample.domain.TgJourney;
import ua.com.fielden.platform.sample.domain.TgJourneyOverNightStay;
import ua.com.fielden.platform.sample.domain.TgJourneyPurpose;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.web.PrefDim.Unit;
import ua.com.fielden.platform.web.action.CentreConfigurationWebUiConfig.CentreConfigActions;
import ua.com.fielden.platform.web.app.config.IWebUiBuilder;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.impl.EntityCentreBuilder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.view.master.EntityMaster;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;

/**
 * {@link TgJourney} Web UI configuration.
 *
 * @author TG Team
 *
 */
public class TgJourneyWebUiConfig {

    public final EntityCentre<TgJourney> centre;
    public final EntityMaster<TgJourney> master;

    public static TgJourneyWebUiConfig register(final Injector injector, final IWebUiBuilder builder) {
        return new TgJourneyWebUiConfig(injector, builder);
    }

    private TgJourneyWebUiConfig(final Injector injector, final IWebUiBuilder builder) {
        centre = createCentre(injector, builder);
        builder.register(centre);
        master = createMaster(injector);
        builder.register(master);
    }

    /**
     * Creates entity centre for {@link TgJourney}.
     *
     * @param injector
     * @return created entity centre
     */
    private EntityCentre<TgJourney> createCentre(final Injector injector, final IWebUiBuilder builder) {
        final String layout = LayoutComposer.mkVarGridForCentre(2, 2, 2, 2, 2, 2, 3, 2, 2, 2, 2);

        final EntityActionConfig standardExportAction = StandardActions.EXPORT_ACTION.mkAction(TgJourney.class);
        final EntityActionConfig standardEditAction = StandardActions.EDIT_ACTION.mkAction(TgJourney.class);
        final EntityActionConfig standardSortAction = CentreConfigActions.CUSTOMISE_COLUMNS_ACTION.mkAction();
        builder.registerOpenMasterAction(TgJourney.class, standardEditAction);

        final EntityCentreConfig<TgJourney> ecc = EntityCentreBuilder.centreFor(TgJourney.class)
                .runAutomatically()
                .addTopAction(standardSortAction).also()
                .addTopAction(standardExportAction)
                .addCrit("machine").asMulti().autocompleter(TgMachine.class).also()
                .addCrit("desc").asMulti().text().also()
                .addCrit("startDate").asRange().dateTime().also()
                .addCrit("finishDate").asRange().dateTime().also()
                .addCrit("startOdometer").asRange().decimal().also()
                .addCrit("finishOdometer").asRange().decimal().also()
                .addCrit("startAddress").asMulti().text().also()
                .addCrit("finishAddress").asMulti().text().also()
                .addCrit("business").asMulti().bool().also()
                .addCrit("distance").asRange().decimal().also()
                .addCrit("businessDistance").asRange().decimal().also()
                .addCrit("privateDistance").asRange().decimal().also()
                .addCrit("driver").asMulti().autocompleter(Person.class).also()
                .addCrit("purpose").asMulti().autocompleter(TgJourneyPurpose.class).also()
                .addCrit("overNightStay").asMulti().autocompleter(TgJourneyOverNightStay.class).also()
                .addCrit("active").asMulti().bool().also()
                .addCrit("gnssOutageFinish").asMulti().bool().also()
                .addCrit("preliminaryFinish").asMulti().bool().also()
                .addCrit("preliminaryFinishResetByIgnitionOn").asMulti().bool().also()
                .addCrit("startLatitude").asRange().decimal().also()
                .addCrit("startLongitude").asRange().decimal().also()
                .addCrit("finishLatitude").asRange().decimal().also()
                .addCrit("finishLongitude").asRange().decimal()
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withScrollingConfig(standardStandaloneScrollingConfig(0))
                .addProp("machine").order(1).asc().minWidth(100).also()
                .addEditableProp("desc").minWidth(100).also()
                .addProp("startDate").minWidth(100)
                    .withSummary("total_count_", "COUNT(SELF)", format("Count:The total number of matching %ss.", TgJourney.ENTITY_TITLE))
                    .also()
                .addProp("finishDate").minWidth(100).also()
                .addProp("startOdometer").minWidth(100).also()
                .addProp("finishOdometer").minWidth(100).also()
                .addEditableProp("startAddress").minWidth(100).also()
                .addEditableProp("finishAddress").minWidth(100).also()
                .addEditableProp("business").minWidth(100).also()
                .addProp("distance").minWidth(100)
                    .withSummary("total_distance_", "SUM(distance)", format("Total Distance:The total distance of matching %ss.", TgJourney.ENTITY_TITLE))
                    .also()
                .addProp("businessDistance").minWidth(100)
                    .withSummary("total_business_distance_", "SUM(businessDistance)", format("Total Business Distance:The total business distance of matching %ss.", TgJourney.ENTITY_TITLE))
                    .also()
                .addProp("privateDistance").minWidth(100)
                    .withSummary("total_private_distance_", "SUM(privateDistance)", format("Total Private Distance:The total private distance of matching %ss.", TgJourney.ENTITY_TITLE))
                    .also()
                .addEditableProp("driver").minWidth(100).also()
                .addEditableProp("purpose").minWidth(100).also()
                .addEditableProp("overNightStay").minWidth(100).also()
                .addProp("active").minWidth(100).also()
                .addProp("gnssOutageFinish").minWidth(100).also()
                .addProp("preliminaryFinish").minWidth(100).also()
                .addProp("preliminaryFinishResetByIgnitionOn").minWidth(100).also()
                .addProp("latestDate").minWidth(100).also()
                .addProp("earliestDate").minWidth(100).also()
                .addProp("startLatitude").minWidth(100).also()
                .addProp("startLongitude").minWidth(100).also()
                .addProp("finishLatitude").minWidth(100).also()
                .addProp("finishLongitude").minWidth(100)
                .addPrimaryAction(standardEditAction)
                .build();

        return new EntityCentre<>(MiTgJourney.class, ecc, injector);
    }

    /**
     * Creates entity master for {@link TgJourney}.
     *
     * @param injector
     * @return created entity master
     */
    private EntityMaster<TgJourney> createMaster(final Injector injector) {
        final String layout = LayoutComposer.mkGridForMasterFitWidth(7, 1);

        final IMaster<TgJourney> masterConfig = new SimpleMasterBuilder<TgJourney>().forEntity(TgJourney.class)
                .addProp("startAddress").asSinglelineText().also()
                .addProp("finishAddress").asSinglelineText().also()
                .addProp("business").asCheckbox().also()
                .addProp("desc").asSinglelineText().also()
                .addProp("driver").asAutocompleter().also()
                .addProp("purpose").asAutocompleter().also()
                .addProp("overNightStay").asAutocompleter().also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE)
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .withDimensions(mkDim(LayoutComposer.SIMPLE_ONE_COLUMN_MASTER_DIM_WIDTH, 480, Unit.PX))
                .done();

        return new EntityMaster<>(TgJourney.class, masterConfig, injector);
    }
}