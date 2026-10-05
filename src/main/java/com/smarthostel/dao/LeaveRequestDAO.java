package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.LeaveRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for leave_requests table.
 */
public class LeaveRequestDAO {

    public List<LeaveRequest> getAllLeaveRequests() throws DatabaseException {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = """
            SELECT l.leave_id, l.student_id, l.from_date, l.to_date, l.reason, l.status, l.approved_by,
                   s.name AS student_name, w.name AS approver_name
            FROM leave_requests l
            JOIN students s ON l.student_id = s.student_id
            LEFT JOIN wardens w ON l.approved_by = w.warden_id
            ORDER BY l.from_date DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLeaveRequest(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve leave requests: " + e.getMessage(), e);
        }
        return list;
    }

    public List<LeaveRequest> getLeaveRequestsByBlock(int blockId) throws DatabaseException {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = """
            SELECT l.leave_id, l.student_id, l.from_date, l.to_date, l.reason, l.status, l.approved_by,
                   s.name AS student_name, w.name AS approver_name
            FROM leave_requests l
            JOIN students s ON l.student_id = s.student_id
            JOIN rooms r ON s.room_id = r.room_id
            LEFT JOIN wardens w ON l.approved_by = w.warden_id
            WHERE r.block_id = ?
            ORDER BY l.from_date DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, blockId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToLeaveRequest(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve leave requests for block: " + e.getMessage(), e);
        }
        return list;
    }

    public List<LeaveRequest> getLeaveRequestsByStudent(int studentId) throws DatabaseException {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = """
            SELECT l.leave_id, l.student_id, l.from_date, l.to_date, l.reason, l.status, l.approved_by,
                   s.name AS student_name, w.name AS approver_name
            FROM leave_requests l
            JOIN students s ON l.student_id = s.student_id
            LEFT JOIN wardens w ON l.approved_by = w.warden_id
            WHERE l.student_id = ?
            ORDER BY l.from_date DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToLeaveRequest(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve leave requests for student: " + e.getMessage(), e);
        }
        return list;
    }

    public LeaveRequest addLeaveRequest(LeaveRequest leaveRequest) throws DatabaseException {
        String sql = """
            INSERT INTO leave_requests (student_id, from_date, to_date, reason, status, approved_by)
            VALUES (?, ?, ?, ?, ?, ?);
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, leaveRequest.getStudentId());
                pstmt.setString(2, leaveRequest.getFromDate());
                pstmt.setString(3, leaveRequest.getToDate());
                pstmt.setString(4, leaveRequest.getReason());
                pstmt.setString(5, leaveRequest.getStatus() != null ? leaveRequest.getStatus() : "PENDING");
                if (leaveRequest.getApprovedBy() != null) {
                    pstmt.setInt(6, leaveRequest.getApprovedBy());
                } else {
                    pstmt.setNull(6, Types.INTEGER);
                }
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        leaveRequest.setLeaveId(generatedKeys.getInt(1));
                    }
                }
                return leaveRequest;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to submit leave request: " + e.getMessage(), e);
        }
    }

    public void updateStatus(int leaveId, String status, Integer approvedBy) throws DatabaseException {
        String sql = "UPDATE leave_requests SET status = ?, approved_by = ? WHERE leave_id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status);
                if (approvedBy != null) {
                    pstmt.setInt(2, approvedBy);
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }
                pstmt.setInt(3, leaveId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update leave request status: " + e.getMessage(), e);
        }
    }

    public int countPending() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM leave_requests WHERE status = 'PENDING';";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count pending leave requests: " + e.getMessage(), e);
        }
        return 0;
    }

    private LeaveRequest mapResultSetToLeaveRequest(ResultSet rs) throws SQLException {
        Integer approverId = (Integer) rs.getObject("approved_by");
        LeaveRequest l = new LeaveRequest(
                rs.getInt("leave_id"),
                rs.getInt("student_id"),
                rs.getString("from_date"),
                rs.getString("to_date"),
                rs.getString("reason"),
                rs.getString("status"),
                approverId
        );
        l.setStudentName(rs.getString("student_name"));
        l.setApprovedByName(rs.getString("approver_name"));
        return l;
    }
}
