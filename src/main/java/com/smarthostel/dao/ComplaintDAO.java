package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.Complaint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for complaints table.
 */
public class ComplaintDAO {

    public List<Complaint> getAllComplaints() throws DatabaseException {
        List<Complaint> list = new ArrayList<>();
        String sql = """
            SELECT c.complaint_id, c.student_id, c.category, c.description, c.room_id, c.status, c.priority,
                   c.created_at, c.resolved_at, s.name AS student_name, r.room_number
            FROM complaints c
            JOIN students s ON c.student_id = s.student_id
            LEFT JOIN rooms r ON c.room_id = r.room_id
            ORDER BY c.created_at DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToComplaint(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve complaints: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Complaint> getComplaintsByBlock(int blockId) throws DatabaseException {
        List<Complaint> list = new ArrayList<>();
        String sql = """
            SELECT c.complaint_id, c.student_id, c.category, c.description, c.room_id, c.status, c.priority,
                   c.created_at, c.resolved_at, s.name AS student_name, r.room_number
            FROM complaints c
            JOIN students s ON c.student_id = s.student_id
            LEFT JOIN rooms r ON c.room_id = r.room_id
            WHERE r.block_id = ?
            ORDER BY c.created_at DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, blockId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToComplaint(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve complaints for block: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Complaint> getComplaintsByStudent(int studentId) throws DatabaseException {
        List<Complaint> list = new ArrayList<>();
        String sql = """
            SELECT c.complaint_id, c.student_id, c.category, c.description, c.room_id, c.status, c.priority,
                   c.created_at, c.resolved_at, s.name AS student_name, r.room_number
            FROM complaints c
            JOIN students s ON c.student_id = s.student_id
            LEFT JOIN rooms r ON c.room_id = r.room_id
            WHERE c.student_id = ?
            ORDER BY c.created_at DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToComplaint(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve complaints for student: " + e.getMessage(), e);
        }
        return list;
    }

    public Complaint addComplaint(Complaint complaint) throws DatabaseException {
        String sql = """
            INSERT INTO complaints (student_id, category, description, room_id, status, priority)
            VALUES (?, ?, ?, ?, ?, ?);
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, complaint.getStudentId());
                pstmt.setString(2, complaint.getCategory());
                pstmt.setString(3, complaint.getDescription());
                if (complaint.getRoomId() != null) {
                    pstmt.setInt(4, complaint.getRoomId());
                } else {
                    pstmt.setNull(4, Types.INTEGER);
                }
                pstmt.setString(5, complaint.getStatus() != null ? complaint.getStatus() : "OPEN");
                pstmt.setString(6, complaint.getPriority() != null ? complaint.getPriority() : "MEDIUM");
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        complaint.setComplaintId(generatedKeys.getInt(1));
                    }
                }
                return complaint;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to submit complaint: " + e.getMessage(), e);
        }
    }

    public void updateStatus(int complaintId, String status) throws DatabaseException {
        String sql = """
            UPDATE complaints
            SET status = ?,
                resolved_at = CASE WHEN ? = 'RESOLVED' THEN CURRENT_TIMESTAMP ELSE resolved_at END
            WHERE complaint_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status);
                pstmt.setString(2, status);
                pstmt.setInt(3, complaintId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update complaint status: " + e.getMessage(), e);
        }
    }

    public int countPending() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM complaints WHERE status != 'RESOLVED';";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count pending complaints: " + e.getMessage(), e);
        }
        return 0;
    }

    private Complaint mapResultSetToComplaint(ResultSet rs) throws SQLException {
        Integer roomId = (Integer) rs.getObject("room_id");
        Complaint c = new Complaint(
                rs.getInt("complaint_id"),
                rs.getInt("student_id"),
                rs.getString("category"),
                rs.getString("description"),
                roomId,
                rs.getString("status"),
                rs.getString("priority"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("resolved_at")
        );
        c.setStudentName(rs.getString("student_name"));
        c.setRoomNumber(rs.getString("room_number"));
        return c;
    }
}
