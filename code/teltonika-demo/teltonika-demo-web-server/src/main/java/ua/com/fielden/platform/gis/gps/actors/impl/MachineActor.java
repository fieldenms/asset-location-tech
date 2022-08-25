package ua.com.fielden.platform.gis.gps.actors.impl;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionImplementor;

import akka.actor.ActorRef;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.gis.gps.actors.Packet;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.sample.domain.ITgMessage;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMessage;

/**
 * This actor is responsible for messages processing for concrete machine.
 *
 * @author TG Team
 *
 */
public class MachineActor extends AbstractAvlMachineActor<TgMessage, TgMachine> {
    private final Logger logger = getLogger(MachineActor.class);

    private final ITgMessage coMessage;

    public MachineActor(final EntityFactory factory, final TgMachine machine, final TgMessage lastMessage, final HibernateUtil hibUtil, final ITgMessage coMessage, final ActorRef machinesCounterRef, final ActorRef violatingMessageResolverRef, final boolean emergencyMode, final int windowSize, final int windowSize2, final int windowSize3, final double averagePacketSizeThreshould, final double averagePacketSizeThreshould2) {
        super(factory, machine, lastMessage, hibUtil, machinesCounterRef, violatingMessageResolverRef, emergencyMode, windowSize, windowSize2, windowSize3, averagePacketSizeThreshould, averagePacketSizeThreshould2);
        this.coMessage = coMessage;

        if (!isEmergencyMode()) {
            try {
                processTempMessages(machine);
            } catch (final Exception e) {
                logger.error("Failed to process message from temporal storage while creating actor for machine [" + machine + "]", e);
                // TODO this error is strictly the problem of developer logic. The server in this case should be strictly stopped and server maintainer notified (SMS, email etc.)
            }
        }
    }

    @Override
    public void processTempMessages(final TgMachine machine) throws Exception {
        final Session session = getHibUtil().getSessionFactory().getCurrentSession();
        final Transaction tr = session.beginTransaction();
        final LinkedList<Packet<TgMessage>> packets = new LinkedList<>();
        final String stm = "SELECT * FROM TEMP_MESSAGES WHERE machine_ = ? ORDER BY packet_";
        final PreparedStatement pst = ((SessionImplementor) session).connection().prepareStatement(stm);

        // Turn use of the cursor on.
        pst.setFetchSize(100);

        pst.setLong(1, machine.getId());
        final ResultSet rs = pst.executeQuery();
        Packet<TgMessage> currPacket = null;
        while (rs.next()) {
            final Date packetRecordValue = rs.getTimestamp("packet_");
            if (currPacket == null || (currPacket != null && currPacket.getCreated() < packetRecordValue.getTime())) {
                currPacket = new Packet<TgMessage>(packetRecordValue, getMessagesComparator());
                packets.add(currPacket);
            }
            final TgMessage message = new TgMessage();
            message.setMachine(machine);
            message.setGpsTime(rs.getTimestamp("gpstime_"));
            message.setX(rs.getBigDecimal("x_"));
            message.setY(rs.getBigDecimal("y_"));
            message.setVectorAngle(rs.getInt("vectorangle_"));
            message.setVectorSpeed(rs.getInt("vectorspeed_"));
            message.setAltitude(rs.getInt("altitude_"));
            message.setVisibleSattelites(rs.getInt("visiblesattelites_"));
            message.setPowerSupplyVoltage(rs.getBigDecimal("powersupplyvoltage_"));
            message.setBatteryVoltage(rs.getBigDecimal("batteryvoltage_"));
            message.setPacketReceived(packetRecordValue);
            message.setDin1(rs.getBoolean("din1_"));
            message.setGpsPower(rs.getBoolean("gpspower_"));

            currPacket.add(message);
        }
        rs.close();
        pst.close();
        tr.commit();

        for (final Packet<TgMessage> packet : packets) {
            processSinglePacket(packet, true);
        }
    }

    @Override
    protected void persist(final Collection<TgMessage> messages, final TgMessage latestPersistedMessage) throws Exception {
        final Session session = getHibUtil().getSessionFactory().getCurrentSession();
        final Transaction tr = session.beginTransaction();
        final PreparedStatement batchInsertStmt = ((SessionImplementor) session).connection().prepareStatement("INSERT INTO MESSAGES (machine_, "
                + "gpstime_, packet_, vectorangle_, vectorspeed_, altitude_, visiblesattelites_, x_, y_, powersupplyvoltage_, batteryvoltage_, din1_, gpspower_, distance_, status_) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        int batchId = 0;
        TgMessage currLatestPersistedMessage = latestPersistedMessage;

        for (final TgMessage message : messages) {
            if (currLatestPersistedMessage != null) {
                message.setTravelledDistance(calcDistance(currLatestPersistedMessage, message));
            }

            assignValuesToParams(batchInsertStmt, message);
            batchInsertStmt.addBatch();

            currLatestPersistedMessage = message;
            batchId = batchId + 1;

            if ((batchId % jdbcInsertBatchSize) == 0) {
                batchInsertStmt.executeBatch();
                batchInsertStmt.clearBatch();
            }
        }

        if ((batchId % jdbcInsertBatchSize) != 0) {
            batchInsertStmt.executeBatch();
            batchInsertStmt.clearBatch();
        }
        batchInsertStmt.close();

        deleteFromTemporal(messages, ((SessionImplementor) session).connection());

        tr.commit();
    }

    @Override
    protected void persistEmergently(final Collection<TgMessage> messages) throws Exception {

        final Session session = getHibUtil().getSessionFactory().getCurrentSession();
        final Transaction tr = session.beginTransaction();
        final PreparedStatement batchInsertStmt = ((SessionImplementor) session).connection().prepareStatement("INSERT INTO EMERGENCY_MESSAGES (machine_, "
                + "gpstime_, packet_, vectorangle_, vectorspeed_, altitude_, visiblesattelites_, x_, y_, powersupplyvoltage_, batteryvoltage_, din1_, gpspower_) "
                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?)");

        for (final TgMessage message : messages) {
            assignValuesToParamsEmergently(batchInsertStmt, message);
            batchInsertStmt.addBatch();
        }

        batchInsertStmt.executeBatch();
        batchInsertStmt.clearBatch();
        batchInsertStmt.close();
        tr.commit();
    }

    private void deleteFromTemporal(final Collection<TgMessage> messages, final Connection conn) throws Exception {
        final TgMessage first = messages.iterator().next();
        TgMessage last = null;
        for (final Iterator<TgMessage> iterator = messages.iterator(); iterator.hasNext();) {
            final TgMessage message = iterator.next();
            if (!iterator.hasNext()) {
                last = message;
            }
        }

        final PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM TEMP_MESSAGES WHERE machine_ = ? and gpstime_ >= ? and gpstime_ <= ?");
        deleteStmt.setObject(1, first.getMachine().getId());
        deleteStmt.setObject(2, new Timestamp(first.getGpsTime().getTime()));
        deleteStmt.setObject(3, new Timestamp(last.getGpsTime().getTime()));
        deleteStmt.executeUpdate();
        deleteStmt.close();
    }

    @Override
    protected void persistTemporarily(final Packet<TgMessage> packet) throws Exception {
        for (final TgMessage message : packet.getMessages()) {
            final Session session = getHibUtil().getSessionFactory().getCurrentSession();
            final Transaction tr = session.beginTransaction();
            final PreparedStatement batchInsertStmt = ((SessionImplementor) session).connection().prepareStatement("INSERT INTO TEMP_MESSAGES (machine_, "
                    + "gpstime_, packet_, vectorangle_, vectorspeed_, altitude_, visiblesattelites_, x_, y_, powersupplyvoltage_, batteryvoltage_, din1_, gpspower_) "
                    + "values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
            assignValuesToParamsTemp(batchInsertStmt, message);
            try {
                batchInsertStmt.executeUpdate();
                batchInsertStmt.close();
                tr.commit();
            } catch (final Exception e) {
                batchInsertStmt.close();
                tr.rollback();
                logger.error("Failed to persist temporary messages from packet [" + packet.getCreated() + "], [" + message.getMachine().getId() + "] ... message [" + message
                        + "] due to " + e, e);
                // TODO this error is strictly the problem of developer logic. The server in this case should be strictly stopped and server maintainer notified (SMS, email etc.)
            }
        }
    }

    protected static void assignValuesToParamsEmergently(final PreparedStatement batchInsertStmt, final TgMessage message) throws Exception {
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
    }

    protected static void assignValuesToParams(final PreparedStatement batchInsertStmt, final TgMessage message) throws Exception {
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
        batchInsertStmt.setObject(14, message.getTravelledDistance());
        batchInsertStmt.setObject(15, message.getStatus());
    }

    protected static void assignValuesToParamsTemp(final PreparedStatement batchInsertStmt, final TgMessage message) throws Exception {
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
