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
    @Title(value = "Start Date", desc = "Approximate starting date / time of the Journey. Usually it is a little bit later (~1 min) after actual start of the movement.")
    @CompositeKeyMember(2)
    @Optional
    private Date startDate;

    @IsProperty
    @MapTo
    @Title(value = "Finish Date", desc = "Approximate finishing date / time of the Journey. Usually it is a little bit later (~1 min) after actual finish of the movement.")
    @CompositeKeyMember(3)
    @Optional
    private Date finishDate;

    @IsProperty(precision = 18, scale = 2)
    @MapTo
    @Title(value = "Start Odometer", desc = "Approximate odometer at starting date / time of the Journey. The accuracy is very much dependent on the accuracy of initial odometer setting when associating machine with module. It is expected to have accuracy close to kilometers.")
    private BigDecimal startOdometer;

    @IsProperty(precision = 18, scale = 2)
    @MapTo
    @Title(value = "Finish Odometer", desc = "Approximate odometer at finishing date / time of the Journey. The accuracy is very much dependent on the accuracy of initial odometer setting when associating machine with module. It is expected to have accuracy close to kilometers.")
    private BigDecimal finishOdometer;

    @IsProperty(length = 255)
    @MapTo
    @Title(value = "Start Address", desc = "Reversely geocoded address (with a postcode) of the Journey's start.")
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private String startAddress;

    @IsProperty(length = 255)
    @MapTo
    @Title(value = "Finish Address", desc = "Reversely geocoded address (with a postcode) of the Journey's finish.")
    @BeforeChange(@Handler(MaxLengthValidator.class))
    private String finishAddress;

    @IsProperty
    @MapTo
    @Title(value = "Business?", desc = "Indicates whether Journey was made for business purposes or not.")
    private boolean business = true;

    @IsProperty(precision = 18, scale = 2)
    @Readonly
    @Calculated
    @Title(value = "Distance", desc = "Approximate distance between starting / finishing dates of the Journey. It is expected to have accuracy close to 1/10 of a kilometer.")
    private BigDecimal distance;
    protected static final ExpressionModel distance_ = expr().prop("finishOdometer").sub().prop("startOdometer").model();

    @IsProperty(precision = 18, scale = 2)
    @Readonly
    @Calculated
    @Title(value = "Business Distance", desc = "Approximate business distance between starting / finishing dates of the Journey. It is expected to have accuracy close to 1/10 of a kilometer.")
    private BigDecimal businessDistance;
    protected static final ExpressionModel businessDistance_ = expr().caseWhen().prop("business").eq().val(true).then().prop("distance").end().model();

    @IsProperty(precision = 18, scale = 2)
    @Readonly
    @Calculated
    @Title(value = "Private Distance", desc = "Approximate private distance between starting / finishing dates of the Journey. It is expected to have accuracy close to 1/10 of a kilometer.")
    private BigDecimal privateDistance;
    protected static final ExpressionModel privateDistance_ = expr().caseWhen().prop("business").eq().val(false).then().prop("distance").end().model();

    @IsProperty
    @MapTo
    @Title(value = "Driver", desc = "The Driver that performed a Journey.")
    private Person driver;

    @IsProperty
    @MapTo
    @Title(value = "Purpose", desc = "Journey purpose.")
    private TgJourneyPurpose purpose;

    @IsProperty
    @MapTo
    @Title(value = "Over Night Stay", desc = "Indicates where Vehicle stayed the last night before Journey.")
    private TgJourneyOverNightStay overNightStay;

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "In Progress?", desc = "Indicates whether the journey is progress and i.e. not yet completed.")
    private boolean active;
    protected static final ExpressionModel active_ = expr().caseWhen().allOfProps("startDate", "finishDate").isNotNull().then().prop("preliminaryFinish").otherwise().val(true).endAsBool().model();

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

    @IsProperty
    @MapTo
    @Title(value = "Preliminary Finish?", desc = "Indicates whether existing Finish Date is a preliminary finish of the Journey i.e. needs additional consequent finishing message within timeout to consider the Journey completed.")
    private boolean preliminaryFinish = false;

    @IsProperty
    @MapTo
    @Title(value = "Preliminary Finish Reset By Ignition On?", desc = "Indicates whether existing preliminary Finish Date was reset by newly appearing Ignition ON messages within timeout.")
    private boolean preliminaryFinishResetByIgnitionOn = false;

    @IsProperty
    @MapTo
    @Title(value = "GNSS Outage Finish?", desc = "Indicates whether Journey finish was caused by GNSS loosing connection i.e. no visible satellites with ignition ON and trip odometer suddenly reset to zero.")
    private boolean gnssOutageFinish = false;

    @IsProperty
    @Readonly
    @Calculated
    private Date latestDate;
    protected static final ExpressionModel latestDate_ = expr().caseWhen().prop("finishDate").isNotNull().then().prop("finishDate").otherwise().prop("startDate").end().model();

    @IsProperty
    @Readonly
    @Calculated
    private Date earliestDate;
    protected static final ExpressionModel earliestDate_ = expr().caseWhen().prop("startDate").isNotNull().then().prop("startDate").otherwise().prop("finishDate").end().model();

    @Observable
    protected TgJourney setEarliestDate(final Date earliestDate) {
        this.earliestDate = earliestDate;
        return this;
    }

    public Date getEarliestDate() {
        return earliestDate;
    }

    @Observable
    protected TgJourney setLatestDate(final Date latestDate) {
        this.latestDate = latestDate;
        return this;
    }

    public Date getLatestDate() {
        return latestDate;
    }

    @Observable
    public TgJourney setGnssOutageFinish(final boolean gnssOutageFinish) {
        this.gnssOutageFinish = gnssOutageFinish;
        return this;
    }

    public boolean isGnssOutageFinish() {
        return gnssOutageFinish;
    }

    @Observable
    public TgJourney setPreliminaryFinishResetByIgnitionOn(final boolean preliminaryFinishResetByIgnitionOn) {
        this.preliminaryFinishResetByIgnitionOn = preliminaryFinishResetByIgnitionOn;
        return this;
    }

    public boolean isPreliminaryFinishResetByIgnitionOn() {
        return preliminaryFinishResetByIgnitionOn;
    }

    @Observable
    public TgJourney setPreliminaryFinish(final boolean preliminaryFinish) {
        this.preliminaryFinish = preliminaryFinish;
        return this;
    }

    public boolean isPreliminaryFinish() {
        return preliminaryFinish;
    }

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

    public boolean isActive() {
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
    protected TgJourney setPrivateDistance(final BigDecimal privateDistance) {
        this.privateDistance = privateDistance;
        return this;
    }

    public BigDecimal getPrivateDistance() {
        return privateDistance;
    }

    @Observable
    protected TgJourney setBusinessDistance(final BigDecimal businessDistance) {
        this.businessDistance = businessDistance;
        return this;
    }

    public BigDecimal getBusinessDistance() {
        return businessDistance;
    }

    @Observable
    protected TgJourney setDistance(final BigDecimal distance) {
        this.distance = distance;
        return this;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    @Observable
    public TgJourney setBusiness(final boolean business) {
        this.business = business;
        return this;
    }

    public boolean isBusiness() {
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
    public TgJourney setStartOdometer(final BigDecimal startOdometer) {
        this.startOdometer = startOdometer;
        return this;
    }

    public BigDecimal getStartOdometer() {
        return startOdometer;
    }

    @Observable
    public TgJourney setFinishOdometer(final BigDecimal finishOdometer) {
        this.finishOdometer = finishOdometer;
        return this;
    }

    public BigDecimal getFinishOdometer() {
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