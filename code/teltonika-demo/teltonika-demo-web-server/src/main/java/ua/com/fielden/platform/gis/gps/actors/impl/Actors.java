package ua.com.fielden.platform.gis.gps.actors.impl;

import java.util.List;
import java.util.Map;

import com.google.inject.Injector;

import akka.actor.ActorRef;
import ua.com.fielden.platform.dao.session.TransactionalExecution;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.sample.domain.ITgMessage;
import ua.com.fielden.platform.sample.domain.TgJourneyCo;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineDriverAssociationCo;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgModule;

/**
 * A container for all actors that maintains messages.
 *
 * @author TG Team
 *
 */
public class Actors extends AbstractActors<TgMessage, TgMachine, TgModule, TgMachineModuleAssociation, MachineActor, ModuleActor> {
    /**
     * Creates an actor system responsible for processing messages and getting efficiently a state from it (e.g. last machine message).
     *
     * @param hibUtil
     *            -- an utility to communicate with DB for message persistence
     * @param machines
     *            -- a current machines in a system (creating of a new machine is not supported yet)
     */
    public Actors(final Injector injector, final Map<TgMachine, TgMessage> machinesWithLastMessages, final Map<TgModule, List<TgMachineModuleAssociation>> modulesWithAssociations, final String gpsHost, final Integer gpsPort) {
        super(injector, machinesWithLastMessages, modulesWithAssociations, gpsHost, gpsPort);
    }

    @Override
    protected MachineActor createMachineActor(final Injector injector, final TgMachine machine, final TgMessage lastMessage, final ActorRef machinesCounterRef) {
        return new MachineActor(
            injector.getInstance(EntityFactory.class),
            machine,
            lastMessage,
            injector.getInstance(TgJourneyCo.class),
            injector.getInstance(TgMachineDriverAssociationCo.class),
            machinesCounterRef,
            new EntityBatchInsertOperation(injector.getInstance(DomainMetadata.class), () -> injector.getInstance(TransactionalExecution.class)),
            injector.getInstance(ITgMessage.class)
        );
    }

    @Override
    protected ModuleActor createModuleActor(final Injector injector, final TgModule module, final List<TgMachineModuleAssociation> associations, final ActorRef modulesCounterRef) {
        return new ModuleActor(
                injector.getInstance(EntityFactory.class),
                module,
                associations,
                injector.getInstance(HibernateUtil.class),
                modulesCounterRef,
                this);
    }

    @Override
    protected void nettyAvlServerStartedPostAction() {
        super.nettyAvlServerStartedPostAction();

//        try {
//            final String event = "porter-server-started";
//            getLogger().info("Emitting '" + event + "' event...");
//            Runtime.getRuntime().exec("./emit-started-event.sh");
//            getLogger().info("Emitted '" + event + "' event.");
//        } catch (final Exception e) {
//            getLogger().error(e.getMessage(), e);
//            throw new IllegalStateException(e);
//        }
    }
}
