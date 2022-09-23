package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachineModuleTemporalAssociation;

@CompanionObject(ITgMachineModuleAssociation.class)
@MapEntityTo
public class TgMachineModuleAssociation extends AbstractAvlMachineModuleTemporalAssociation<TgMessage, TgMachine, TgModule> {
    @IsProperty
    @Title("Vehicle")
    @MapTo
    @CompositeKeyMember(1)
    private TgMachine machine;

    @IsProperty
    @MapTo
    @Title(value = "Module", desc = "Module, that is associated with the Vehicle.")
    @CompositeKeyMember(2)
    private TgModule module;

    @IsProperty
    @MapTo
    @Title(value = "Init Odometer", desc = "Initial Odometer (in a kilometers) when association started.")
    @Required
    private Integer initOdometer;

    @Observable
    public TgMachineModuleAssociation setInitOdometer(final Integer initOdometer) {
        this.initOdometer = initOdometer;
        return this;
    }

    public Integer getInitOdometer() {
        return initOdometer;
    }

    @Override
    public TgModule getModule() {
        return module;
    }

    @Override
    @Observable
    public TgMachineModuleAssociation setModule(final TgModule module) {
        this.module = module;
        return this;
    }

    @Override
    public TgMachine getMachine() {
        return machine;
    }

    @Override
    @Observable
    public TgMachineModuleAssociation setMachine(final TgMachine machine) {
        this.machine = machine;
        return this;
    }
}