package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.SecurityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for security_logs table.
 */
public class SecurityLogDAO {

    public List<SecurityLog> getAllLogs() throws DatabaseException {
        List<SecurityLog> list = new ArrayList<>();
        String sql = """
            SELECT sl.log_id, sl.student_id, sl.event_type, sl.event_time, sl.gate, sl.verification_method,
                   s.name AS student_name
            FROM security_logs sl
            LEFT JOIN students s ON sl.student_id = s.student_id
            ORDER BY sl.event_time DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSecurityLog(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve security logs: " + e.getMessage(), e);
        }
        return list;
    }

    public List<SecurityLog> getRecentLogs(int limit) throws DatabaseException {
        List<SecurityLog> list = new ArrayList<>();
        String sql = """
            SELECT sl.log_id, sl.student_id, sl.event_type, sl.event_time, sl.gate, sl.verification_method,
                   s.name AS student_name
            FROM security_logs sl
            LEFT JOIN students s ON sl.student_id = s.student_id
            ORDER BY sl.event_time DESC
            LIMIT ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToSecurityLog(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve recent security logs: " + e.getMessage(), e);
        }
        return list;
    }

    public SecurityLog addLog(SecurityLog log) throws DatabaseException {
        String sql = """
            INSERT INTO security_logs (student_id, event_type, event_time, gate, verification_method)
            VALUES (?, ?, ?, ?, ?);
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                if (log.getStudentId() != null) {
                    pstmt.setInt(1, log.getStudentId());
                } else {
                    pstmt.setNull(1, Types.INTEGER);
                }
                pstmt.setString(2, log.getEventType());
                pstmt.setTimestamp(3, log.getEventTime());
                pstmt.setString(4, log.getGate());
                pstmt.setString(5, log.getVerificationMethod());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        log.setLogId(generatedKeys.getInt(1));
                    }
                }
                return log;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to record security log: " + e.getMessage(), e);
        }
    }

    public int countAlerts() throws DatabaseException {
        // Late night entries (after 10 PM) or unregistered events
        String sql = """
            SELECT COUNT(*) FROM security_logs
            WHERE TIME(event_time) > '22:00:00' OR TIME(event_time) < '05:00:00';
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count security alerts: " + e.getMessage(), e);
        }
        return 0;
    }

    private SecurityLog mapResultSetToSecurityLog(ResultSet rs) throws SQLException {
        Integer studentId = (Integer) rs.getObject("student_id");
        SecurityLog log = new SecurityLog(
                rs.getInt("log_id"),
                studentId,
                rs.getString("event_type"),
                rs.getTimestamp("event_time"),
                rs.getString("gate"),
                rs.getString("verification_method")
        );
        String name = rs.getString("student_name");
        log.setStudentName(name != null ? name : "Visitor / Guest");

        // Determine if entry was late (e.g. after 22:00 / 10:00 PM)
        if (log.getEventTime() != null) {
            int hours = log.getEventTime().toLocalDateTime().getHour();
            if (hours >= 22 || hours < 5) {
                log.setStatus("LATE");
            } else {
                log.setStatus("NORMAL");
            }
        } else {
            log.setStatus("NORMAL");
        }
        return log;
    }
}
