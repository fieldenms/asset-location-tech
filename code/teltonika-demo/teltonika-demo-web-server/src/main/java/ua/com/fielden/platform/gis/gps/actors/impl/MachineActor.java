package ua.com.fielden.platform.gis.gps.actors.impl;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.dao.HibernateMappingsGenerator.ID_SEQUENCE_NAME;
import static ua.com.fielden.platform.gis.gps.actors.impl.JourneyProcessor.createJourneysFrom;
import static ua.com.fielden.platform.utils.DbUtils.nextIdValue;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;

import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionImplementor;

import akka.actor.ActorRef;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.persistence.HibernateUtil;
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

    private final TgJourneyCo journeyCo;
    private final TgMachineDriverAssociationCo machineDriverAssociationCo;

    public MachineActor(final EntityFactory factory, final TgMachine machine, final TgMessage latestGpsMessage, final HibernateUtil hibUtil, final TgJourneyCo journeyCo, final TgMachineDriverAssociationCo machineDriverAssociationCo, final ActorRef machinesCounterRef) {
        super(factory, machine, latestGpsMessage, hibUtil, machinesCounterRef);
        this.journeyCo = journeyCo;
        this.machineDriverAssociationCo = machineDriverAssociationCo;
    }

    @Override
    protected void persist(final Collection<TgMessage> messages) throws Exception {
//        final Session session = getHibUtil().getSessionFactory().getCurrentSession();
//        final Transaction tr = session.beginTransaction();
//        final PreparedStatement batchInsertStmt = ((SessionImplementor) session).connection().prepareStatement("INSERT INTO MESSAGES (machine_, "
//                + "gpstime_, packet_, vectorangle_, vectorspeed_, altitude_, visiblesattelites_, x_, y_, powersupplyvoltage_, batteryvoltage_, din1_, gpspower_, _id, ignition_, totalodometer_, tripodometer_, trip_) "
//                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
//
//        int batchId = 0;
//
//        for (final TgMessage message : messages) {
//            assignValuesToParams(batchInsertStmt, message, session);
//            batchInsertStmt.addBatch();
//            batchId = batchId + 1;
//            if ((batchId % jdbcInsertBatchSize) == 0) {
//                batchInsertStmt.executeBatch();
//                batchInsertStmt.clearBatch();
//            }
//        }
//
//        if ((batchId % jdbcInsertBatchSize) != 0) {
//            batchInsertStmt.executeBatch();
//            batchInsertStmt.clearBatch();
//        }
//        batchInsertStmt.close();
//
//        deleteFromTemporal(messages, ((SessionImplementor) session).connection());
//
//        tr.commit();
        
        for (final TgMessage message : messages) {
            final Session session = getHibUtil().getSessionFactory().getCurrentSession();
            final Transaction tr = session.beginTransaction();
            final PreparedStatement batchInsertStmt = ((SessionImplementor) session).connection().prepareStatement("INSERT INTO MESSAGES (machine_, "
                    + "gpstime_, packet_, vectorangle_, vectorspeed_, altitude_, visiblesattelites_, x_, y_, powersupplyvoltage_, batteryvoltage_, din1_, gpspower_, _id, ignition_, totalodometer_, tripodometer_, trip_) "
                    + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            assignValuesToParams(batchInsertStmt, message, session);
            try {
                batchInsertStmt.executeUpdate();
                batchInsertStmt.close();
                tr.commit();
            } catch (final Exception e) {
                batchInsertStmt.close();
                tr.rollback();
                logger.error("Failed to persist temporary messages from vehicle id [" + message.getMachine().getId() + "] ... message [" + message + "] due to " + e, e);
                // TODO this error is strictly the problem of developer logic. The server in this case should be strictly stopped and server maintainer notified (SMS, email etc.)
            }
        }

        // process messages immediately and create / update Journeys from them
        createJourneysFrom(messages, getMachine(), journeyCo, machineDriverAssociationCo);
    }

    protected static void assignValuesToParams(final PreparedStatement batchInsertStmt, final TgMessage message, final Session session) throws Exception {
        batchInsertStmt.setObject(1, message.getMachine().getId());
        batchInsertStmt.setObject(2, new Timestamp(message.getGpsTime().getTime()));
        batchInsertStmt.setObject(3, new Timestamp(message.getPacketReceived().getTime()));
        batchInsertStmt.setObject(4, message.getVectorAngle());
        batchInsertStmt.setObject(5, message.getVectorSpeed());
        batchInsertStmt.setObject(6, message.getAltitude());
        batchInsertStmt.setObject(7, message.getVisibleSattelites());
        batchInsertStmt.setObject(8, message.getX());
        batchInsertStmt.setObject(9, message.getY());
        batchInsertStmt.setObject(10, message.getPowerSupplyVoltage());
        batchInsertStmt.setObject(11, message.getBatteryVoltage());
        batchInsertStmt.setObject(12, message.getDin1() ? "Y" : "N");
        batchInsertStmt.setObject(13, message.getGpsPower() ? "Y" : "N");
        batchInsertStmt.setObject(14, nextIdValue(ID_SEQUENCE_NAME, session));
        batchInsertStmt.setObject(15, message.getIgnition() ? "Y" : "N");
        batchInsertStmt.setObject(16, message.getTotalOdometer());
        batchInsertStmt.setObject(17, message.getTripOdometer());
        batchInsertStmt.setObject(18, message.isTrip() ? "Y" : "N");
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
