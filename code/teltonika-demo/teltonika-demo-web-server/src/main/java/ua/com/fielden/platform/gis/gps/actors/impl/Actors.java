package ua.com.fielden.platform.gis.gps.actors.impl;

import java.util.List;
import java.util.Map;

import com.google.inject.Injector;

import akka.actor.ActorRef;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.sample.domain.ITgMessage;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgModule;

/**
 * A container for all actors that maintains messages.
 *
 * @author TG Team
 *
 */
public class Actors extends AbstractActors<TgMessage, TgMachine, TgModule, TgMachineModuleAssociation, MachineActor, ModuleActor, ViolatingMessageResolverActor> {
    /**
     * Creates an actor system responsible for processing messages and getting efficiently a state from it (e.g. last machine message).
     *
     * @param hibUtil
     *            -- an utility to communicate with DB for message persistence
     * @param machines
     *            -- a current machines in a system (creating of a new machine is not supported yet)
     */
    public Actors(final Injector injector, final Map<TgMachine, TgMessage> machinesWithLastMessages, final Map<TgModule, List<TgMachineModuleAssociation>> modulesWithAssociations, final String gpsHost, final Integer gpsPort, final boolean emergencyMode, final int windowSize, final int windowSize2, final int windowSize3, final double averagePacketSizeThreshould, final double averagePacketSizeThreshould2) {
        super(injector, machinesWithLastMessages, modulesWithAssociations, gpsHost, gpsPort, emergencyMode, windowSize, windowSize2, windowSize3, averagePacketSizeThreshould, averagePacketSizeThreshould2);
    }

    @Override
    protected MachineActor createMachineActor(final Injector injector, final TgMachine machine, final TgMessage lastMessage, final ActorRef machinesCounterRef, final ActorRef violatingMessageResolverRef) {
        return new MachineActor(
                injector.getInstance(EntityFactory.class),
                machine,
                lastMessage,
                injector.getInstance(HibernateUtil.class),
                injector.getInstance(ITgMessage.class),
                machinesCounterRef,
                violatingMessageResolverRef,
                isEmergencyMode(),
                windowSize(),
                windowSize2(),
                windowSize3(),
                averagePacketSizeThreshould(),
                averagePacketSizeThreshould2());
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
    protected ViolatingMessageResolverActor createViolatingMessageResolverActor(final Injector injector) {
        return new ViolatingMessageResolverActor(injector.getInstance(HibernateUtil.class), injector.getInstance(ITgMessage.class));
    }

    @Override
    protected void nettyServerStartedPostAction() {
        super.nettyServerStartedPostAction();

        try {
            final String event = "porter-server-started";
            getLogger().info("Emitting '" + event + "' event...");
            Runtime.getRuntime().exec("./emit-started-event.sh");
            getLogger().info("Emitted '" + event + "' event.");
        } catch (final Exception e) {
            getLogger().error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }
}
