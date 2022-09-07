package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import java.math.BigDecimal;
import java.util.Date;

import fielden.personnel.Person;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Optional;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.MaxLengthValidator;
import ua.com.fielden.platform.utils.Pair;

/**
 * Entity that represents {@link TgMachine} journey with start/finish places and automatically calculated distance.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@CompanionObject(TgJourneyCo.class)
@MapEntityTo
@DescTitle("Description")
public class TgJourney extends AbstractPersistentEntity<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(TgJourney.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @Title("Vehicle")
    @CompositeKeyMember(1)
    private TgMachine machine;

    @IsProperty
    @MapTo
    @Title(value = "Start Date", desc = "Extended_description_1")
    @CompositeKeyMember(2)
    @Optional
    private Date startDate;

    @IsProperty
    @MapTo
    @Title(value = "Finish Date", desc = "Extended_description_2")
    @CompositeKeyMember(3)
    @Optional
    private Date finishDate;

    @IsProperty
    @MapTo
    @Title(value = "Start Odometer", desc = "Extended_description_1")
    private Integer startOdometer;

    @IsProperty
    @MapTo
    @Title(value = "Finish Odometer", desc = "Extended_description_2")
    private Integer finishOdometer;

    @IsProperty(length = 255)
    @MapTo
    @Title(value = "Start Address", desc = "Extended_description")
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private String startAddress;

    @IsProperty(length = 255)
    @MapTo
    @Title(value = "Finish Address", desc = "Extended_description")
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private String finishAddress;

    @IsProperty
    @MapTo
    @Title(value = "Business?", desc = "Extended_description")
    private boolean business = true;

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Distance", desc = "Extended_description")
    private Integer distance;
    protected static final ExpressionModel distance_ = expr().prop("finishOdometer").sub().prop("startOdometer").model();

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Business Distance", desc = "Extended_description")
    private Integer businessDistance;
    protected static final ExpressionModel businessDistance_ = expr().caseWhen().prop("business").eq().val(true).then().prop("distance").endAsInt().model();

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Private Distance", desc = "Extended_description")
    private Integer privateDistance;
    protected static final ExpressionModel privateDistance_ = expr().caseWhen().prop("business").eq().val(false).then().prop("distance").endAsInt().model();

    @IsProperty
    @MapTo
    @Title(value = "Driver", desc = "Extended_description")
    private Person driver;

    @IsProperty
    @MapTo
    @Title(value = "Purpose", desc = "Extended_description")
    private TgJourneyPurpose purpose;

    @IsProperty
    @MapTo
    @Title(value = "Over Night Stay", desc = "Extended_description")
    private TgJourneyOverNightStay overNightStay;

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "In Progress?", desc = "Indicates whether the journey is progress and i.e. not yet completed.")
    private boolean active;
    protected static final ExpressionModel active_ = expr().caseWhen().allOfProps("startDate", "finishDate").isNotNull().then().val(false).otherwise().val(true).endAsBool().model();

    @IsProperty(precision = 18, scale = 10)
    @MapTo
    private BigDecimal startLatitude;

    @IsProperty(precision = 18, scale = 10)
    @MapTo
    private BigDecimal startLongitude;

    @IsProperty(precision = 18, scale = 10)
    @MapTo
    private BigDecimal finishLatitude;

    @IsProperty(precision = 18, scale = 10)
    @MapTo
    private BigDecimal finishLongitude;

    @Observable
    public TgJourney setFinishLongitude(final BigDecimal value) {
        this.finishLongitude = value;
        return this;
    }

    public BigDecimal getFinishLongitude() {
        return finishLongitude;
    }

    @Observable
    public TgJourney setFinishLatitude(final BigDecimal value) {
        this.finishLatitude = value;
        return this;
    }

    public BigDecimal getFinishLatitude() {
        return finishLatitude;
    }

    @Observable
    public TgJourney setStartLongitude(final BigDecimal value) {
        this.startLongitude = value;
        return this;
    }

    public BigDecimal getStartLongitude() {
        return startLongitude;
    }

    @Observable
    public TgJourney setStartLatitude(final BigDecimal value) {
        this.startLatitude = value;
        return this;
    }

    public BigDecimal getStartLatitude() {
        return startLatitude;
    }

    @Observable
    protected TgJourney setActive(final boolean active) {
        this.active = active;
        return this;
    }

    public boolean getActive() {
        return active;
    }

    @Observable
    public TgJourney setOverNightStay(final TgJourneyOverNightStay overNightStay) {
        this.overNightStay = overNightStay;
        return this;
    }

    public TgJourneyOverNightStay getOverNightStay() {
        return overNightStay;
    }

    @Observable
    public TgJourney setPurpose(final TgJourneyPurpose purpose) {
        this.purpose = purpose;
        return this;
    }

    public TgJourneyPurpose getPurpose() {
        return purpose;
    }

    @Observable
    public TgJourney setDriver(final Person driver) {
        this.driver = driver;
        return this;
    }

    public Person getDriver() {
        return driver;
    }

    @Observable
    protected TgJourney setPrivateDistance(final Integer privateDistance) {
        this.privateDistance = privateDistance;
        return this;
    }

    public Integer getPrivateDistance() {
        return privateDistance;
    }

    @Observable
    protected TgJourney setBusinessDistance(final Integer businessDistance) {
        this.businessDistance = businessDistance;
        return this;
    }

    public Integer getBusinessDistance() {
        return businessDistance;
    }

    @Observable
    protected TgJourney setDistance(final Integer distance) {
        this.distance = distance;
        return this;
    }

    public Integer getDistance() {
        return distance;
    }

    @Observable
    public TgJourney setBusiness(final boolean business) {
        this.business = business;
        return this;
    }

    public boolean getBusiness() {
        return business;
    }

    @Observable
    public TgJourney setFinishAddress(final String finishAddress) {
        this.finishAddress = finishAddress;
        return this;
    }

    public String getFinishAddress() {
        return finishAddress;
    }

    @Observable
    public TgJourney setStartAddress(final String startAddress) {
        this.startAddress = startAddress;
        return this;
    }

    public String getStartAddress() {
        return startAddress;
    }

    @Observable
    public TgJourney setStartOdometer(final Integer startOdometer) {
        this.startOdometer = startOdometer;
        return this;
    }

    public Integer getStartOdometer() {
        return startOdometer;
    }

    @Observable
    public TgJourney setFinishOdometer(final Integer finishOdometer) {
        this.finishOdometer = finishOdometer;
        return this;
    }

    public Integer getFinishOdometer() {
        return finishOdometer;
    }

    @Observable
    public TgJourney setStartDate(final Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public Date getStartDate() {
        return startDate;
    }

    @Observable
    public TgJourney setFinishDate(final Date finishDate) {
        this.finishDate = finishDate;
        return this;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    @Observable
    public TgJourney setMachine(final TgMachine machine) {
        this.machine = machine;
        return this;
    }

    public TgMachine getMachine() {
        return machine;
    }

}