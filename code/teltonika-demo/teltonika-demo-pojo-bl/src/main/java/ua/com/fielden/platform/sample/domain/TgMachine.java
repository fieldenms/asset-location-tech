package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@CompanionObject(ITgMachine.class)
@MapEntityTo
public class TgMachine extends AbstractAvlMachine<TgMessage> {
    @IsProperty
    @MapTo
    @Title(value = "Орг. підрозділ", desc = "Організаційний підрозділ, до якого належить машина")
    private TgOrgUnit orgUnit;
    
    @IsProperty(linkProperty = "machine")
    @Readonly
    @Calculated
    @Title(value = "Останнє GPS повідомлення", desc = "Містить інформацію про останнє GPS повідомлення, отримане від GPS модуля.")
    private TgMessage lastMessage;
    
    private static ExpressionModel lastMessage_ = 
        expr().model(
            select(TgMessage.class)
            .where().prop(TgMessage.MACHINE_PROP_ALIAS).eq().extProp("id")
            .and().notExists(
                select(TgMessage.class)
                .where().prop(TgMessage.MACHINE_PROP_ALIAS).eq().extProp(TgMessage.MACHINE_PROP_ALIAS)
                .and().prop("gpsTime").gt().extProp("gpsTime").model()
            )            
            .model()
        ).model();
    
    @Observable
    public TgMachine setOrgUnit(final TgOrgUnit orgUnit) {
        this.orgUnit = orgUnit;
        return this;
    }
    
    public TgOrgUnit getOrgUnit() {
        return orgUnit;
    }

    @Override
    @Observable
    public TgMachine setLastMessage(final TgMessage lastMessage) {
        this.lastMessage = lastMessage;
        return this;
    }

    @Override
    public TgMessage getLastMessage() {
        return lastMessage;
    }
}