package ua.com.fielden.platform.gis.gps.actors.impl;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;

import akka.actor.ActorRef;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlModuleActor;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgModule;

/**
 * This actor is responsible for messages processing for concrete module.
 *
 * @author TG Team
 *
 */
public class ModuleActor extends AbstractAvlModuleActor<TgMessage, TgMachine, TgModule, TgMachineModuleAssociation> {
    private final Logger logger = getLogger(ModuleActor.class);

    public ModuleActor(final EntityFactory factory, final TgModule module, final List<TgMachineModuleAssociation> machineAssociations, final HibernateUtil hibUtil, final ActorRef modulesCounterRef, final AbstractActors<?, ?, ?, ?, ?, ?, ?> actors) {
        super(factory, module, machineAssociations, hibUtil, modulesCounterRef, actors);
    }

    @Override
    protected TgMessage createMessage() {
        return new TgMessage();
    }

    @Override
    protected TgMessage setMachineForMessage(final TgMessage message, final TgMachine activeMachine) {
        message.setMachine(activeMachine);
        return message;
    }

    @Override
    protected TgMachineModuleAssociation createSampleModuleAssociation(final Date date) {
        final TgMachineModuleAssociation association = new TgMachineModuleAssociation();
        association.setFrom(date);
        return association;
    }
}