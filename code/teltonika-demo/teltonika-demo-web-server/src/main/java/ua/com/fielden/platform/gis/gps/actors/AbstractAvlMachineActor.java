package ua.com.fielden.platform.gis.gps.actors;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import org.apache.logging.log4j.Logger;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.gis.MapUtils;
import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.persistence.HibernateUtil;

/**
 * This actor is responsible for messages processing for concrete machine.
 * 
 * @author TG Team
 * 
 */
public abstract class AbstractAvlMachineActor<MESSAGE extends AbstractAvlMessage, MACHINE extends AbstractAvlMachine<MESSAGE>> extends UntypedActor {
    private final MessagesComparator<MESSAGE> messagesComparator;
    protected static int jdbcInsertBatchSize = 100;
    protected static final Logger LOGGER = getLogger(AbstractAvlMachineActor.class);

    private MACHINE machine;
    private MESSAGE latestGpsMessage;
    private final HibernateUtil hibUtil;
    private ActorRef machinesCounterRef;

    public AbstractAvlMachineActor(final EntityFactory factory, final MACHINE machine, final MESSAGE latestGpsMessage, final HibernateUtil hibUtil, final ActorRef machinesCounterRef) {
        this.machinesCounterRef = machinesCounterRef;
        this.messagesComparator = new MessagesComparator<MESSAGE>();
        this.machine = machine;
        this.latestGpsMessage = latestGpsMessage;
        this.hibUtil = hibUtil;
    }

    @Override
    public void preStart() {
        super.preStart();

        machinesCounterRef.tell(new MachineActorStarted(this.machine.getKey(), this.machine.getDesc()), getSelf());
        machinesCounterRef = null;
    }

    protected abstract void persist(final Collection<MESSAGE> messages) throws Exception;

    protected final void processSinglePacket(final Packet<MESSAGE> originalPacket, final boolean onStart) throws Exception {
        final Packet<MESSAGE> packet = originalPacket;
        if (!packet.isEmpty()) {
            if (latestGpsMessage == null || latestGpsMessage.getGpsTime().getTime() < packet.getFinish().getGpsTime().getTime()) {
                final MESSAGE oldLatestGpsMessage = latestGpsMessage;
                latestGpsMessage = packet.getFinish();
                processLatestGpsMessage(oldLatestGpsMessage, latestGpsMessage);
            }
            if (!onStart) {
                persist(packet.getMessages());
            }
        }
    }

    /**
     * Provides custom processing action after new 'latest GPS message' has been arrived.
     * 
     * @param oldLatestGpsMessage
     * @param newLatestGpsMessage
     */
    protected void processLatestGpsMessage(final MESSAGE oldLatestGpsMessage, final MESSAGE newLatestGpsMessage) {
    }

    @Override
    public void onReceive(final Object data) throws Exception {
        try {
            if (data instanceof Packet) {
                final Packet<MESSAGE> packet = (Packet<MESSAGE>) data;
                for (final MESSAGE message : packet.getMessages()) {
                    completeMessage(message);
                }
                processSinglePacket(packet, false);
            } else if (data instanceof final LastMessagesRequest lastMessageRequest) {
                if (latestGpsMessage != null
                        && (lastMessageRequest.getAfterDate() == null || latestGpsMessage.getGpsTime().getTime() > lastMessageRequest.getAfterDate().getTime())) {
                    final MESSAGE lastMessage = completeMessageCopy(produceIncompleteLastMessage(latestGpsMessage), latestGpsMessage);
                    getSender().tell(new LastMessage<MESSAGE>(lastMessageRequest.getMachineId(), lastMessage), getSelf());
                } else {
                    getSender().tell(new NoLastMessage(), getSelf());
                }
            } else if (data instanceof Changed) {
                promoteChangedMachine((Changed<MACHINE>) data);
            } else {
                unhandled(data);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    protected void promoteChangedMachine(final Changed<MACHINE> changedMachine) {
        setMachine(changedMachine.getValue());
        LOGGER.info("An existent machine, that has been changed [" + changedMachine.getValue() + "], has been sucessfully promoted to its machine actor.");
    }

    private MESSAGE produceIncompleteLastMessage(final MESSAGE message) {
        final MESSAGE copy = createMessage();
        copy.setX(message.getX());
        copy.setY(message.getY());
        copy.setVectorSpeed(message.getVectorSpeed());
        copy.setVectorAngle(message.getVectorAngle());
        copy.setGpsTime(message.getGpsTime());
        return copy;
    }

    public final static <MESSAGE extends AbstractAvlMessage> BigDecimal calcDistance(final MESSAGE prevMessage, final MESSAGE currMessage) {
        return new BigDecimal(MapUtils.calcDistance(prevMessage.getX(), //
                prevMessage.getY(), //
                currMessage.getX(), //
                currMessage.getY())).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * A method to create empty GPS message.
     * 
     * @return
     */
    protected abstract MESSAGE createMessage();

    /**
     * A method to fill GPS message with client-specific data.
     * 
     * @param message
     * @return
     */
    protected abstract MESSAGE completeMessage(final MESSAGE message);

    /**
     * A method to fill GPS message with client-specific data.
     * 
     * @param populateData
     * @return
     */
    protected abstract MESSAGE completeMessageCopy(final MESSAGE populateData, final MESSAGE messageToCopyFrom);

    protected MACHINE getMachine() {
        return machine;
    }

    protected void setMachine(final MACHINE machine) {
        this.machine = machine;
    }

    protected HibernateUtil getHibUtil() {
        return hibUtil;
    }

    public MessagesComparator<MESSAGE> getMessagesComparator() {
        return messagesComparator;
    }

}