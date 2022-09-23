package ua.com.fielden.platform.sample.domain;

import java.util.Date;

import fielden.personnel.Person;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.security.user.User;

@CompanionObject(TgMachineDriverAssociationCo.class)
@MapEntityTo
@KeyType(DynamicEntityKey.class)
@KeyTitle("Vehicle Association with Driver")
@DescTitle("Comments")
@EntityTitle("Vehicle Association with Driver")
public class TgMachineDriverAssociation extends AbstractEntity<DynamicEntityKey> {
    @IsProperty
    @Title("Vehicle")
    @MapTo
    @CompositeKeyMember(1)
    private TgMachine machine;

    @IsProperty
    @MapTo
    @Title(value = "Driver", desc = "Driver associated with the Vehicle.")
    @CompositeKeyMember(2)
    private Person driver;

    @IsProperty
    @MapTo
    @Dependent("to")
    @Title(value = "From Date", desc = "The date when association started.")
    @CompositeKeyMember(3)
    private Date from;

    @IsProperty
    @MapTo
    @Dependent("from")
    @Title(value = "To Date", desc = "The date when association finished.")
    private Date to;

    @IsProperty(assignBeforeSave = true)
    @MapTo
    @Title(value = "Creation Date", desc = "The date when association was created.")
    private Date created;

    @IsProperty
    @MapTo
    @Title(value = "Changed Date", desc = "The date when association was changed.")
    private Date changed;

    @IsProperty(assignBeforeSave = true)
    @MapTo
    @Title(value = "Created By", desc = "The user, that associated the Vehicle with the Driver.")
    @Readonly
    private User createdBy;

    @IsProperty
    @MapTo
    @Title(value = "Changed By", desc = "The user, that changed Vehicle association with the Driver.")
    // @TransactionUser
    // @Required
    @Readonly
    private User changedBy;

    @Observable
    public TgMachineDriverAssociation setChangedBy(final User changedBy) {
        this.changedBy = changedBy;
        return this;
    }

    public User getChangedBy() {
        return changedBy;
    }

    @Observable
    public TgMachineDriverAssociation setCreatedBy(final User createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    @Observable
    public TgMachineDriverAssociation setChanged(final Date changed) {
        this.changed = changed;
        return this;
    }

    public Date getChanged() {
        return changed;
    }

    @Observable
    public TgMachineDriverAssociation setCreated(final Date created) {
        this.created = created;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    @Observable
    @LeProperty("to")
    public TgMachineDriverAssociation setFrom(final Date from) {
        this.from = from;
        return this;
    }

    public Date getFrom() {
        return from;
    }

    @Observable
    @GeProperty("from")
    public TgMachineDriverAssociation setTo(final Date to) {
        this.to = to;
        return this;
    }

    public Date getTo() {
        return to;
    }

    public Person getDriver() {
        return driver;
    }

    @Observable
    public TgMachineDriverAssociation setDriver(final Person driver) {
        this.driver = driver;
        return this;
    }

    public TgMachine getMachine() {
        return machine;
    }

    @Observable
    public TgMachineDriverAssociation setMachine(final TgMachine machine) {
        this.machine = machine;
        return this;
    }

}