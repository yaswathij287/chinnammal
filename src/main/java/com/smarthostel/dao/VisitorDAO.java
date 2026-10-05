package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.Visitor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for visitors table.
 */
public class VisitorDAO {

    public List<Visitor> getAllVisitors() throws DatabaseException {
        List<Visitor> list = new ArrayList<>();
        String sql = """
            SELECT v.visitor_id, v.student_id, v.visitor_name, v.relationship, v.phone,
                   v.visit_date, v.visit_time, v.status, s.name AS student_name
            FROM visitors v
            JOIN students s ON v.student_id = s.student_id
            ORDER BY v.visit_date DESC, v.visitor_id DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToVisitor(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve visitors: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Visitor> getVisitorsByBlock(int blockId) throws DatabaseException {
        List<Visitor> list = new ArrayList<>();
        String sql = """
            SELECT v.visitor_id, v.student_id, v.visitor_name, v.relationship, v.phone,
                   v.visit_date, v.visit_time, v.status, s.name AS student_name
            FROM visitors v
            JOIN students s ON v.student_id = s.student_id
            JOIN rooms r ON s.room_id = r.room_id
            WHERE r.block_id = ?
            ORDER BY v.visit_date DESC, v.visitor_id DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, blockId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToVisitor(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve visitors for block: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Visitor> getVisitorsByStudent(int studentId) throws DatabaseException {
        List<Visitor> list = new ArrayList<>();
        String sql = """
            SELECT v.visitor_id, v.student_id, v.visitor_name, v.relationship, v.phone,
                   v.visit_date, v.visit_time, v.status, s.name AS student_name
            FROM visitors v
            JOIN students s ON v.student_id = s.student_id
            WHERE v.student_id = ?
            ORDER BY v.visit_date DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToVisitor(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve visitor requests for student: " + e.getMessage(), e);
        }
        return list;
    }

    public Visitor addVisitor(Visitor visitor) throws DatabaseException {
        String sql = """
            INSERT INTO visitors (student_id, visitor_name, relationship, phone, visit_date, visit_time, status)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, visitor.getStudentId());
                pstmt.setString(2, visitor.getVisitorName());
                pstmt.setString(3, visitor.getRelationship());
                pstmt.setString(4, visitor.getPhone());
                pstmt.setString(5, visitor.getVisitDate());
                pstmt.setString(6, visitor.getVisitTime());
                pstmt.setString(7, visitor.getStatus() != null ? visitor.getStatus() : "PENDING");
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        visitor.setVisitorId(generatedKeys.getInt(1));
                    }
                }
                return visitor;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add visitor request: " + e.getMessage(), e);
        }
    }

    public void updateStatus(int visitorId, String status) throws DatabaseException {
        String sql = "UPDATE visitors SET status = ? WHERE visitor_id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status);
                pstmt.setInt(2, visitorId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update visitor status: " + e.getMessage(), e);
        }
    }

    public int countPending() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM visitors WHERE status = 'PENDING';";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count pending visitors: " + e.getMessage(), e);
        }
        return 0;
    }

    private Visitor mapResultSetToVisitor(ResultSet rs) throws SQLException {
        Visitor v = new Visitor(
                rs.getInt("visitor_id"),
                rs.getInt("student_id"),
                rs.getString("visitor_name"),
                rs.getString("relationship"),
                rs.getString("phone"),
                rs.getString("visit_date"),
                rs.getString("visit_time"),
                rs.getString("status")
        );
        v.setStudentName(rs.getString("student_name"));
        return v;
    }
}
