package ua.com.fielden.platform.gis.gps.actors.impl;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.sql.PreparedStatement;
import java.util.Collection;

import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionImplementor;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.gis.gps.actors.AbstractViolatingMessageResolverActor;
import ua.com.fielden.platform.gis.gps.actors.Packet;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.sample.domain.ITgMessage;
import ua.com.fielden.platform.sample.domain.TgMessage;

/**
 * This actors corrects the violating messages for all machines. The heavy-weight logic of correcting violating messages has been separated from machine actor to prevent
 * simultaneous hard-disk-intensive database operations and corrections.
 *
 * @author TG Team
 *
 */
public class ViolatingMessageResolverActor extends AbstractViolatingMessageResolverActor<TgMessage> {
    private final Logger logger = getLogger(ViolatingMessageResolverActor.class);

    private final ITgMessage coMessage;

    public ViolatingMessageResolverActor(final HibernateUtil hibUtil, final ITgMessage coMessage) {
        super(hibUtil);
        this.coMessage = coMessage;
    }

    @Override
    protected void processViolators(final Packet<TgMessage> packet) throws Exception {
        final DateTime st = new DateTime();
        persistViolatingMessagesEmergently(packet.getMessages());
//        for (final Message message : packet.getMessages()) {
//            // persistError(message);
//            correctError(message, coMessage);
//        }
        final Period pd = new Period(st, new DateTime());
        logger.info("Persisted violating messages [" + packet.getMessages().size() + "] for [" + packet.getMessages().first().getMachine() + "]. Duration: " + durationStr(pd) + ".");
    }

    private String durationStr(final Period p) {
        return (p.getHours() == 0 ? "" : p.getHours() + " h ") + (p.getMinutes() == 0 ? "" : p.getMinutes() + " m ") + p.getSeconds() + " s " + p.getMillis() + " ms";
    }

    protected void persistViolatingMessagesEmergently(final Collection<TgMessage> messages) throws Exception {

        final Session session = getHibUtil().getSessionFactory().getCurrentSession();
        final Transaction tr = session.beginTransaction();
        final PreparedStatement batchInsertStmt = ((SessionImplementor) session).connection().prepareStatement("INSERT INTO VIOLATING_MESSAGES (machine_, "
                + "gpstime_, packet_, vectorangle_, vectorspeed_, altitude_, visiblesattelites_, x_, y_, powersupplyvoltage_, batteryvoltage_, din1_, gpspower_, _id, ignition_, totalodometer_, tripodometer_, trip_) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        for (final TgMessage message : messages) {
            MachineActor.assignValuesToParamsEmergently(batchInsertStmt, message, session);
            batchInsertStmt.addBatch();
        }

        batchInsertStmt.executeBatch();
        batchInsertStmt.clearBatch();
        batchInsertStmt.close();
        tr.commit();
    }
    
    protected void persistViolatingMessage(final TgMessage message, final TgMessage latestPersistedMessage, final TgMessage nextMessage) throws Exception {
        final DateTime st = new DateTime();

        final Session session = getHibUtil().getSessionFactory().getCurrentSession();
        final Transaction tr = session.beginTransaction();
        final PreparedStatement insertStmt = ((SessionImplementor) session).connection().prepareStatement("INSERT INTO MESSAGES (machine_, "
                + "gpstime_, packet_, vectorangle_, vectorspeed_, altitude_, visiblesattelites_, x_, y_, powersupplyvoltage_, batteryvoltage_, din1_, gpspower_, distance_, status_, _id, ignition_, totalodometer_, tripodometer_, trip_) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        message.setTravelledDistance(AbstractAvlMachineActor.calcDistance(latestPersistedMessage, message));
        message.setStatus(2);
        MachineActor.assignValuesToParams(insertStmt, message, session);
        insertStmt.executeUpdate();

        final PreparedStatement updateStmt = ((SessionImplementor) session).connection().prepareStatement("UPDATE MESSAGES SET distance_ = ?, _version = _version + 1 where _id = ?");
        updateStmt.setObject(1, AbstractAvlMachineActor.calcDistance(message, nextMessage));
        updateStmt.setObject(2, nextMessage.getId());
        updateStmt.executeUpdate();

        tr.commit();

        final Period p = new Period(st, new DateTime());

        if (p.getMillis() > 10000) {
            logger.info(" Persisting of violating message for machine " + message.getMachine().getKey() + " took "
                    + (p.getHours() == 0 ? "" : p.getHours() + " h " + (p.getMinutes() == 0 ? "" : p.getMinutes() + " m ") + p.getSeconds() + " s " + p.getMillis() + " ms."));
        }
    }

    private TgMessage getPrevMessage(final TgMessage achronologicalMessage, final boolean ignited, final ITgMessage coMessage) {
        final EntityResultQueryModel<TgMessage> query = select(TgMessage.class).where(). //
            prop("din1").eq().iVal(ignited ? ignited : null). //
            and(). //
            prop("machine").eq().val(achronologicalMessage.getMachine()). //
            and(). //
            prop("gpsTime").lt().val(achronologicalMessage.getGpsTime()). //
            and(). //
            notExists(select(TgMessage.class).where(). //
            prop("din1").eq().iVal(ignited ? ignited : null). //
            and(). //
            prop("machine").eq().extProp("machine"). //
            and(). //
            prop("gpsTime").lt().val(achronologicalMessage.getGpsTime()). //
            and(). //
            prop("gpsTime").gt().extProp("gpsTime").model())
            .model();
        return coMessage.getEntity(from(query).model());
    }

    private TgMessage getNextMessage(final TgMessage achronologicalMessage, final boolean ignited, final ITgMessage coMessage) {
        final EntityResultQueryModel<TgMessage> query = select(TgMessage.class).where(). //
            prop("din1").eq().iVal(ignited ? ignited : null). //
            and(). //
            prop("machine").eq().val(achronologicalMessage.getMachine()). //
            and(). //
            prop("gpsTime").gt().val(achronologicalMessage.getGpsTime()). //
            and(). //
            notExists(select(TgMessage.class).where(). //
            prop("din1").eq().iVal(ignited ? ignited : null). //
            and(). //
            prop("machine").eq().extProp("machine"). //
            and(). //
            prop("gpsTime").gt().val(achronologicalMessage.getGpsTime()). //
            and(). //
            prop("gpsTime").lt().extProp("gpsTime").model())
            .model();
        return coMessage.getEntity(from(query).model());
    }

    private void persistError(final TgMessage message) throws Exception {
        final Session session = getHibUtil().getSessionFactory().getCurrentSession();
        final Transaction tr = session.beginTransaction();
        final PreparedStatement batchInsertStmt = ((SessionImplementor) session).connection().prepareStatement("INSERT INTO BAD_MESSAGES (machine_, "
                + "gpstime_, packet_, vectorangle_, vectorspeed_, altitude_, visiblesattelites_, x_, y_, powersupplyvoltage_, batteryvoltage_, din1_, gpspower_, _id, ignition_, totalodometer_, tripodometer_, trip_) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        MachineActor.assignValuesToParamsTemp(batchInsertStmt, message, session);
        try {
            batchInsertStmt.executeUpdate();
            batchInsertStmt.close();
            tr.commit();
        } catch (final Exception e) {
            batchInsertStmt.close();
            tr.rollback();
            logger.error("Failed to persist bad messages from packet [" + message.getPacketReceived() + "], [" + message.getMachine().getId() + "] ... message ["
                    + message + "] due to " + e, e);
            // TODO this error is strictly the problem of developer logic. The server in this case should be strictly stopped and server maintainer notified (SMS, email etc.)
        }
    }

    private void correctError(final TgMessage errorMessage, final ITgMessage coMessage) throws Exception {
        final TgMessage prevMessage = getPrevMessage(errorMessage, false, coMessage);
        final TgMessage nextMessage = getNextMessage(errorMessage, false, coMessage);
        final TgMessage existingMessage = coMessage.findByEntityAndFetch(null, errorMessage);

        if (prevMessage == null) {
            persistError(errorMessage);
        } else if (existingMessage == null) {
            persistViolatingMessage(errorMessage, prevMessage, nextMessage);
        } else {
            persistError(errorMessage);
        }
    }
}
