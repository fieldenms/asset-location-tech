package ua.com.fielden.platform.gis.gps.actors.impl;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.VERSION;
import static ua.com.fielden.platform.gis.gps.actors.impl.JourneyProcessor.createJourneysFrom;
import static ua.com.fielden.platform.utils.EntityUtils.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import akka.actor.ActorRef;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.EntityBatchInsertOperation;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.sample.domain.ITgMessage;
import ua.com.fielden.platform.sample.domain.TgJourneyCo;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineDriverAssociationCo;
import ua.com.fielden.platform.sample.domain.TgMessage;

/**
 * This actor is responsible for messages processing for concrete machine.
 *
 * @author TG Team
 *
 */
public class MachineActor extends AbstractAvlMachineActor<TgMessage, TgMachine> {
    private final Logger logger = getLogger(MachineActor.class);

    private final ITgMessage messageCo;
    private final TgJourneyCo journeyCo;
    private final TgMachineDriverAssociationCo machineDriverAssociationCo;
    private final EntityBatchInsertOperation insertOp;

    public MachineActor(final EntityFactory factory, final TgMachine machine, final TgMessage latestGpsMessage, final TgJourneyCo journeyCo, final TgMachineDriverAssociationCo machineDriverAssociationCo, final ActorRef machinesCounterRef, final EntityBatchInsertOperation insertOp, final ITgMessage messageCo) {
        super(factory, machine, latestGpsMessage, machinesCounterRef);
        this.journeyCo = journeyCo;
        this.machineDriverAssociationCo = machineDriverAssociationCo;
        this.insertOp = insertOp;
        this.messageCo = messageCo;
    }

    @Override
    protected void persist(final Collection<TgMessage> messagesColl) throws Exception {
        // TODO don't forget to do changeSubject.publish for manually batch-inserted messages that are saved not through companion 'save' methods
        final var messages = new ArrayList<>(messagesColl);
        final var messagesForJourneys = new ArrayList<TgMessage>();
        if (messages.size() == 1) {
            persist(messages.get(0)).ifPresent(msg -> messagesForJourneys.add(msg));
        } else {
            try {
                insertOp.batchInsert(messages, 1000 /* ensure all messages to be in a single batch; there are, typically, 25 messages in big packets */);
                messagesForJourneys.addAll(messages);
            } catch (final Exception exAll) {
                logger.warn(format("Failed to batch insert messages (%s) for vehicle [%s] due to [%s]. Will try to insert one-by-one.", messages.size(), getMachine(), exAll), exAll);
                for (final var message : messages) {
                    persist(message).ifPresent(msg -> messagesForJourneys.add(msg));
                }
            }
        }
        // process messages immediately and create / update Journeys from them
        createJourneysFrom(messagesForJourneys, getMachine(), journeyCo, machineDriverAssociationCo);
    }

    private Optional<TgMessage> persist(final TgMessage message) {
        try {
            insertOp.batchInsert(asList(message), 1 /* insert one-by-one */);
            return of(message);
        } catch (final Exception exOne) {
            logger.warn(format("Failed to insert message for vehicle [%s] due to [%s]. Will try to resolve as potential duplicate.\nmsg: %s", getMachine(), exOne, message.toStringFull()), exOne);
            try {
                final var prevMessageOpt = messageCo.findByEntityAndFetchOptional(messageCo.getFetchProvider().fetchModel(), message);
                if (prevMessageOpt.isPresent()) {
                    final var prevMessage = prevMessageOpt.get();
                    logger.warn(format("Trying to overwrite duplicate message for vehicle [%s].\nduplicate: %s\nprevious : %s", getMachine(), message.toStringFull(), prevMessage.toStringFull()));
                    copy(message, prevMessage, ID, VERSION, "packetReceived");
                    if (prevMessage.isDirty()) {
                        logger.warn(format("Duplicate message for vehicle [%s] has dirty properties (%s).\nduplicate: %s",
                            getMachine(), prevMessage.getDirtyProperties().stream().map(mp -> mp.getName()).collect(toList()), prevMessage.toStringFull()
                        ));
                        messageCo.save(prevMessage, empty());
                        logger.warn(format("Duplicate message for vehicle [%s] has been saved successfully.\nduplicate: %s", getMachine(), prevMessage.toStringFull()));
                        return of(prevMessage);
                    } else {
                        logger.warn(format("Duplicate message for vehicle [%s] was exactly the same as previous -- skipped.\nduplicate: %s", getMachine(), prevMessage.toStringFull()));
                        return empty();
                    }
                } else {
                    logger.error(format("The message for vehicle [%s] is not a duplicate. The exception needs attention.\nmsg: %s", getMachine(), message.toStringFull()), exOne);
                    return empty();
                }
            } catch (final Exception exOneDuplicateHandle) {
                logger.error(format("Failed to resolve message as potential duplicate for vehicle [%s] due to [%s].\nmsg: %s", getMachine(), exOneDuplicateHandle, message.toStringFull()), exOneDuplicateHandle);
                return empty();
            }
        }
    }

    @Override
    protected TgMessage createMessage() {
        return new TgMessage();
    }

    @Override
    protected TgMessage completeMessage(final TgMessage msg) {
        msg.setMachine(getMachine());
        return msg;
    }

    @Override
    protected TgMessage completeMessageCopy(final TgMessage copy, final TgMessage messageToCopyFrom) {
        return copy.setMachine(getMachine());
    }
}
