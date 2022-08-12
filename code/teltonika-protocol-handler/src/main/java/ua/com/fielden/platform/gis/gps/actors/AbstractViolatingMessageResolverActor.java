package ua.com.fielden.platform.gis.gps.actors;

import static org.apache.logging.log4j.LogManager.getLogger;

import org.apache.logging.log4j.Logger;

import akka.actor.UntypedActor;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.persistence.HibernateUtil;

/**
 * This actors corrects the violating messages for all machines. The heavy-weight logic of correcting violating messages has been separated from machine actor to prevent
 * simultaneous hard-disk-intensive database operations and corrections.
 *
 */
public abstract class AbstractViolatingMessageResolverActor<MESSAGE extends AbstractAvlMessage> extends UntypedActor {
    protected static final Logger LOGGER = getLogger(AbstractViolatingMessageResolverActor.class);

    private final HibernateUtil hibUtil;

    public AbstractViolatingMessageResolverActor(final HibernateUtil hibUtil) {
        this.hibUtil = hibUtil;
    }

    /**
     * Processes a packet with violating messages.
     *
     * @param packet
     * @throws Exception
     */
    protected abstract void processViolators(final Packet<MESSAGE> packet) throws Exception;

    @Override
    public void onReceive(final Object data) throws Exception {
        try {
            if (data instanceof Packet) {
                final Packet<MESSAGE> packetWithViolatingMessages = (Packet<MESSAGE>) data;
                processViolators(packetWithViolatingMessages);
            } else {
                unhandled(data);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    protected HibernateUtil getHibUtil() {
        return hibUtil;
    }

}