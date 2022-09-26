package ua.com.fielden.platform.gis.gps;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.Ignore;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.TransactionEntity;

/**
 * A common base entity for GPS message used for GPS GIS systems server and UI logic.
 * 
 * @author TG Team
 * 
 */
@EntityTitle(value = "GPS повідомлення", desc = "Повідомлення з GPS модуля")
@KeyTitle(value = "GPS message", desc = "Повідомлення з GPS модуля")
@KeyType(DynamicEntityKey.class)
@MapEntityTo("MESSAGES")
// TODO do not forget to provide companion object in its descendants -- @CompanionObject(IMessage.class)
@TransactionEntity("packetReceived")
public abstract class AbstractAvlMessage extends AbstractEntity<DynamicEntityKey> {
    // TODO public static final String MACHINE_PROP_ALIAS = "machineRouteDriver.machine";

    @IsProperty
    @MapTo
    @Title(value = "GPS time", desc = "Час, коли було згенеровано повідомлення")
    @CompositeKeyMember(2)
    private Date gpsTime;

    @IsProperty(precision = 18, scale = 10)
    @MapTo
    @Title(value = "Longitude", desc = "Значення довготи")
    private BigDecimal x;

    @IsProperty(precision = 18, scale = 10)
    @MapTo
    @Title(value = "Latitude", desc = "Значення широти")
    private BigDecimal y;

    @IsProperty
    @MapTo
    @Title(value = "Angle", desc = "Кут повороту машини по відношенню до півночі.")
    private Integer vectorAngle;

    @IsProperty
    @MapTo
    @Title(value = "Speed", desc = "Точкова швидкість руху машину")
    private Integer vectorSpeed;

    @IsProperty
    @MapTo
    @Title(value = "Altitude", desc = "Висота над рівнем моря.")
    private Integer altitude;

    @IsProperty
    @MapTo
    @Title(value = "Sattelites", desc = "Кількість супутників, видимих у момент генерування повідомлення.")
    private Integer visibleSattelites;

    @IsProperty
    @MapTo
    @Title(value = "Din1", desc = "Вказує, чи двигун працював у момент генерування повідомлення.")
    private boolean din1;

    @IsProperty(precision = 18, scale = 2)
    @MapTo
    @Title(value = "Vehicle Battery Voltage", desc = "Вольтаж блоку живлення.")
    private BigDecimal powerSupplyVoltage;

    @IsProperty(precision = 18, scale = 2)
    @MapTo
    @Title(value = "Battery Voltage", desc = "Вольтаж акумулятора.")
    private BigDecimal batteryVoltage;

    @IsProperty
    @MapTo
    @Title(value = "GNSS ON with fix?", desc = "Indicates whether GNSS module is ON with coordinates fix in place.")
    private boolean gpsPower;

    @IsProperty
    @Ignore
    @MapTo("packet_")
    @Title(value = "Packet received date")
    private Date packetReceived;

    @IsProperty
    @MapTo
    @Title("Ignition?")
    private boolean ignition;

    @IsProperty
    @MapTo
    @Title(value = "Total Odometer", desc = "Total odometer reading at the time of the message, in meters.")
    private Integer totalOdometer;

    @IsProperty
    @MapTo
    @Title(value = "Trip Odometer", desc = "Difference between Total Odometer reading at the time of the message and Total Odometer from a) previous message or b) trip start message, in meters.")
    private Integer tripOdometer;

    @IsProperty
    @MapTo
    @Title("Trip?")
    private boolean trip;

    @Observable
    public AbstractAvlMessage setTrip(final boolean value) {
        this.trip = value;
        return this;
    }

    public boolean isTrip() {
        return trip;
    }

    @Observable
    public AbstractAvlMessage setTripOdometer(final Integer value) {
        this.tripOdometer = value;
        return this;
    }

    public Integer getTripOdometer() {
        return tripOdometer;
    }

    @Observable
    public AbstractAvlMessage setTotalOdometer(final Integer value) {
        this.totalOdometer = value;
        return this;
    }

    public Integer getTotalOdometer() {
        return totalOdometer;
    }

    @Observable
    public AbstractAvlMessage setIgnition(final boolean ignition) {
        this.ignition = ignition;
        return this;
    }

    public boolean getIgnition() {
        return ignition;
    }

    @Observable
    public AbstractAvlMessage setPacketReceived(final Date packetReceived) {
        this.packetReceived = packetReceived;
        return this;
    }

    public Date getPacketReceived() {
        return packetReceived;
    }

    @Observable
    public AbstractAvlMessage setGpsPower(final boolean gpsPower) {
        this.gpsPower = gpsPower;
        return this;
    }

    public boolean getGpsPower() {
        return gpsPower;
    }

    @Observable
    public AbstractAvlMessage setBatteryVoltage(final BigDecimal batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
        return this;
    }

    public BigDecimal getBatteryVoltage() {
        return batteryVoltage;
    }

    @Observable
    public AbstractAvlMessage setPowerSupplyVoltage(final BigDecimal powerSupplyVoltage) {
        this.powerSupplyVoltage = powerSupplyVoltage;
        return this;
    }

    public BigDecimal getPowerSupplyVoltage() {
        return powerSupplyVoltage;
    }

    @Observable
    public AbstractAvlMessage setDin1(final boolean din1) {
        this.din1 = din1;
        return this;
    }

    public boolean getDin1() {
        return din1;
    }

    @Observable
    public AbstractAvlMessage setVisibleSattelites(final Integer visibleSattelites) {
        this.visibleSattelites = visibleSattelites;
        return this;
    }

    public Integer getVisibleSattelites() {
        return visibleSattelites;
    }

    @Observable
    public AbstractAvlMessage setAltitude(final Integer altitude) {
        this.altitude = altitude;
        return this;
    }

    public Integer getAltitude() {
        return altitude;
    }

    @Observable
    public AbstractAvlMessage setVectorSpeed(final Integer vectorSpeed) {
        this.vectorSpeed = vectorSpeed;
        return this;
    }

    public Integer getVectorSpeed() {
        return vectorSpeed;
    }

    @Observable
    public AbstractAvlMessage setVectorAngle(final Integer vectorAngle) {
        this.vectorAngle = vectorAngle;
        return this;
    }

    public Integer getVectorAngle() {
        return vectorAngle;
    }

    @Observable
    public AbstractAvlMessage setGpsTime(final Date gpsTime) {
        this.gpsTime = gpsTime;
        return this;
    }

    public Date getGpsTime() {
        return gpsTime;
    }

    @Observable
    public AbstractAvlMessage setY(final BigDecimal y) {
        this.y = y;
        return this;
    }

    public BigDecimal getY() {
        return y;
    }

    @Observable
    public AbstractAvlMessage setX(final BigDecimal x) {
        this.x = x;
        return this;
    }

    public BigDecimal getX() {
        return x;
    }
}