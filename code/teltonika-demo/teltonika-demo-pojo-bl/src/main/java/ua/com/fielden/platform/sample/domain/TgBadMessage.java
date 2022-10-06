package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@CompanionObject(ITgBadMessage.class)
@MapEntityTo("BAD_MESSAGES")
public class TgBadMessage extends AbstractAvlMessage {
    public static final String MACHINE_PROP_ALIAS = "machine";

    @IsProperty
    @MapTo
    @Title(value = "Машина", desc = "Машина, з якої було отримано повідомлення")
    @CompositeKeyMember(1)
    private TgMachine machine;

    @IsProperty(precision = 18, scale = 2)
    @MapTo(value = "distance_")
    // TODO
    @Readonly
    // @Required -- not required, only for TgMessage it is required
    @Title(value = "Відстань", desc = "Відстань в метрах, яку було пройдено машиною з моменту отримання попереднього повідомлення.")
    private BigDecimal travelledDistance;

    @Override
    @Observable
    public AbstractAvlMessage setTravelledDistance(final BigDecimal travelledDistance) {
        this.travelledDistance = travelledDistance;
        return this;
    }

    @Override
    public BigDecimal getTravelledDistance() {
        return travelledDistance;
    }

    @Observable
    public TgBadMessage setMachine(final TgMachine machine) {
        this.machine = machine;
        return this;
    }

    public TgMachine getMachine() {
        return machine;
    }
}