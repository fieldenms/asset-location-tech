package fielden.webapp;

import static org.apache.logging.log4j.LogManager.getLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.gis.MapUtils;
import ua.com.fielden.platform.utils.Pair;

public class StartupSpike {
    private static final Logger LOGGER = getLogger(StartupSpike.class);
    public static int FETCH_SIZE = 500000;
    public static int INSERT_BATCH_SIZE = 10000;

    public static String copyAllMessagesFromTempToEmergencySql = "INSERT INTO EMERGENCY_MESSAGES SELECT * FROM TEMP_MESSAGES";

    public static String deleteAllMessagesFromTempSql = "DELETE FROM TEMP_MESSAGES";

    public static String copyViolatingMessagesSql = "INSERT INTO VIOLATING_MESSAGES " //
            + "SELECT TM.* FROM " + "EMERGENCY_MESSAGES TM, " //
            + "(SELECT MACHINE_, MAX(GPSTIME_) LP FROM MESSAGES M GROUP BY MACHINE_) LP " //
            + "WHERE LP.MACHINE_ = TM.MACHINE_ AND TM.GPSTIME_ <= LP.LP";

    public static String removeCopiedViolatingMessagesSql = "DELETE FROM EMERGENCY_MESSAGES WHERE EXISTS (SELECT * FROM VIOLATING_MESSAGES WHERE MACHINE_ = EMERGENCY_MESSAGES.MACHINE_ AND GPSTIME_ = EMERGENCY_MESSAGES.GPSTIME_)";

    public static String removeDuplicateMessagesSql = "DELETE FROM EMERGENCY_MESSAGES EM1 WHERE EXISTS (SELECT * FROM EMERGENCY_MESSAGES EM2 WHERE EM1.MACHINE_ = EM2.MACHINE_ AND EM1.GPSTIME_ = EM2.GPSTIME_ AND EM1.PACKET_ < EM2.PACKET_)";

    public static String lastMessageCoordinatesSql = "SELECT M.MACHINE_, M.X_, M.Y_ " //
            + "FROM MESSAGES M, " //
            + "(SELECT MACHINE_, MAX(GPSTIME_) MM FROM MESSAGES GROUP BY MACHINE_) LMPM " //
            + "WHERE LMPM.MACHINE_ = M.MACHINE_ AND LMPM.MM = GPSTIME_ ORDER BY M.MACHINE_";

    public static String emergencyMessagesReadSql = "SELECT * FROM EMERGENCY_MESSAGES ORDER BY MACHINE_, GPSTIME_ ASC";

    public static String batchMessageInsertSql = "INSERT INTO MESSAGES (machine_, " //
            + "gpstime_, packet_, vectorangle_, vectorspeed_, altitude_, visiblesattelites_, x_, y_, powersupplyvoltage_, batteryvoltage_, din1_, gpspower_, distance_, status_) " //
            + "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public static String deleteAllEmergencyMessagesSql = "DELETE FROM EMERGENCY_MESSAGES";

    public static void start(final String connectionUrl, final String user, final String password) throws Exception {
        LOGGER.info("Started startup database processing!");

        Class.forName("org.postgresql.Driver");
        final Connection readConn = DriverManager.getConnection(connectionUrl, user, password);
        final Connection writeConn = DriverManager.getConnection(connectionUrl, user, password);
        readConn.setAutoCommit(false);
        writeConn.setAutoCommit(false);

        final Statement correctStmt = writeConn.createStatement();
        final int result1 = correctStmt.executeUpdate(copyAllMessagesFromTempToEmergencySql);
        LOGGER.info("Copied " + result1 + " messages from TEMP_MESSAGES into EMERGENCY_MESSAGES.");
        final int result2 = correctStmt.executeUpdate(deleteAllMessagesFromTempSql);
        LOGGER.info("Deleted just copied " + result2 + " messages from TEMP_MESSAGES.");
        writeConn.commit();

        final int result3 = correctStmt.executeUpdate(copyViolatingMessagesSql);
        LOGGER.info("Copied " + result3 + " violating messages from EMERGENCY_MESSAGES into VIOLATING_MESSAGES.");
        final int result4 = correctStmt.executeUpdate(removeCopiedViolatingMessagesSql);
        LOGGER.info("Deleted just copied " + result4 + " violating messages from EMERGENCY_MESSAGES.");
        writeConn.commit();

        final int result5 = correctStmt.executeUpdate(removeDuplicateMessagesSql);
        LOGGER.info("Deleted duplicate " + result5 + " messages from EMERGENCY_MESSAGES.");
        writeConn.commit();

        final Statement stmt = readConn.createStatement();
        stmt.setFetchSize(FETCH_SIZE);

        final Map<Long, Pair<BigDecimal, BigDecimal>> lastMessageCoordinates = new HashMap<>();
        final ResultSet rsLM = stmt.executeQuery(lastMessageCoordinatesSql);
        while (rsLM.next()) {
            lastMessageCoordinates.put(rsLM.getLong("machine_"), new Pair<BigDecimal, BigDecimal>(rsLM.getBigDecimal("x_"), rsLM.getBigDecimal("y_")));
        }
        rsLM.close();
        LOGGER.info("Retrieved last persisted message coordinates for machines from MESSAGES.");

        final ResultSet rs = stmt.executeQuery(emergencyMessagesReadSql);

        long prevMessageMachine = -1;
        BigDecimal prevMessageX = null;
        BigDecimal prevMessageY = null;

        final PreparedStatement batchInsertStmt = writeConn.prepareStatement(batchMessageInsertSql);

        int batchId = 0;

        while (rs.next()) {

            final long currMessageMachine = rs.getLong("machine_");
            final BigDecimal currMessageX = rs.getBigDecimal("x_");
            final BigDecimal currMessageY = rs.getBigDecimal("y_");

            if (prevMessageMachine != currMessageMachine) {
                final Pair<BigDecimal, BigDecimal> firstMessageCoordinates = lastMessageCoordinates.get(currMessageMachine);
                prevMessageX = firstMessageCoordinates != null ? firstMessageCoordinates.getKey() : currMessageX;
                prevMessageY = firstMessageCoordinates != null ? firstMessageCoordinates.getValue() : currMessageY;
            }

            final BigDecimal distance = new BigDecimal(MapUtils.calcDistance(prevMessageX, prevMessageY, currMessageX, currMessageY)).setScale(2, RoundingMode.HALF_UP);

            batchInsertStmt.setObject(1, currMessageMachine);
            batchInsertStmt.setObject(2, rs.getTimestamp("gpstime_"));
            batchInsertStmt.setObject(3, rs.getTimestamp("packet_"));
            batchInsertStmt.setObject(4, rs.getInt("vectorangle_"));
            batchInsertStmt.setObject(5, rs.getInt("vectorspeed_"));
            batchInsertStmt.setObject(6, rs.getInt("altitude_"));
            batchInsertStmt.setObject(7, rs.getInt("visiblesattelites_"));
            batchInsertStmt.setObject(8, currMessageX);
            batchInsertStmt.setObject(9, currMessageY);
            batchInsertStmt.setObject(10, rs.getBigDecimal("powersupplyvoltage_"));
            batchInsertStmt.setObject(11, rs.getBigDecimal("batteryvoltage_"));
            batchInsertStmt.setObject(12, rs.getString("din1_"));
            batchInsertStmt.setObject(13, rs.getString("gpspower_"));
            batchInsertStmt.setObject(14, distance);
            batchInsertStmt.setObject(15, 3);
            batchInsertStmt.addBatch();

            batchId = batchId + 1;

            if ((batchId % INSERT_BATCH_SIZE) == 0) {
                batchInsertStmt.executeBatch();
                batchInsertStmt.clearBatch();
                writeConn.commit();
                LOGGER.info("Inserted " + batchId + " messages into MESSAGES.");
            }

            prevMessageMachine = currMessageMachine;
            prevMessageX = currMessageX;
            prevMessageY = currMessageY;
        }

        if ((batchId % INSERT_BATCH_SIZE) != 0) {
            batchInsertStmt.executeBatch();
            batchInsertStmt.clearBatch();
            writeConn.commit();
            LOGGER.info("Finished insertion of " + batchId + " messages into MESSAGES.");
        }

        batchInsertStmt.close();

        final int result6 = correctStmt.executeUpdate(deleteAllEmergencyMessagesSql);
        LOGGER.info("Deleted " + result6 + " messages from EMERGENCY_MESSAGES.");
        writeConn.commit();

        correctStmt.close();

        rs.close();
        stmt.close();
        readConn.close();
        writeConn.close();
        LOGGER.info("Finished startup database processing!");
    }
}