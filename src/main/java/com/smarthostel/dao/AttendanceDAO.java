package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.Attendance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for attendance table.
 */
public class AttendanceDAO {

    public List<Attendance> getAllAttendance() throws DatabaseException {
        List<Attendance> list = new ArrayList<>();
        String sql = """
            SELECT a.attendance_id, a.student_id, a.date, a.entry_time, a.exit_time, a.status,
                   s.name AS student_name
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            ORDER BY a.date DESC, a.attendance_id DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve attendance logs: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Attendance> getAttendanceByBlock(int blockId, String date) throws DatabaseException {
        List<Attendance> list = new ArrayList<>();
        String sql = """
            SELECT a.attendance_id, a.student_id, a.date, a.entry_time, a.exit_time, a.status,
                   s.name AS student_name
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            JOIN rooms r ON s.room_id = r.room_id
            WHERE r.block_id = ? AND (? IS NULL OR a.date = ?)
            ORDER BY a.date DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, blockId);
                pstmt.setString(2, date);
                pstmt.setString(3, date);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToAttendance(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve block attendance: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Attendance> getAttendanceByStudent(int studentId) throws DatabaseException {
        List<Attendance> list = new ArrayList<>();
        String sql = """
            SELECT a.attendance_id, a.student_id, a.date, a.entry_time, a.exit_time, a.status,
                   s.name AS student_name
            FROM attendance a
            JOIN students s ON a.student_id = s.student_id
            WHERE a.student_id = ?
            ORDER BY a.date DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToAttendance(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve student attendance history: " + e.getMessage(), e);
        }
        return list;
    }

    public Attendance markAttendance(Attendance attendance) throws DatabaseException {
        String sql = """
            INSERT INTO attendance (student_id, date, entry_time, exit_time, status)
            VALUES (?, ?, ?, ?, ?);
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, attendance.getStudentId());
                pstmt.setString(2, attendance.getDate());
                pstmt.setString(3, attendance.getEntryTime());
                pstmt.setString(4, attendance.getExitTime());
                pstmt.setString(5, attendance.getStatus());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        attendance.setAttendanceId(generatedKeys.getInt(1));
                    }
                }
                return attendance;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to record attendance: " + e.getMessage(), e);
        }
    }

    public double getStudentAttendancePercentage(int studentId) throws DatabaseException {
        String sql = """
            SELECT
                COUNT(*) as total,
                SUM(CASE WHEN status IN ('PRESENT', 'LATE') THEN 1 ELSE 0 END) as attended
            FROM attendance
            WHERE student_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt("total");
                        int attended = rs.getInt("attended");
                        if (total == 0) return 100.0; // Default when fresh
                        return Math.round(((double) attended / total * 100.0) * 10.0) / 10.0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate student attendance percentage: " + e.getMessage(), e);
        }
        return 100.0;
    }

    public int countTodayPresent(String date) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE date = ? AND status IN ('PRESENT', 'LATE');";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, date);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count today's attendance: " + e.getMessage(), e);
        }
        return 0;
    }

    public int countTodayLate(String date) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE date = ? AND status = 'LATE';";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, date);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count today's late entries: " + e.getMessage(), e);
        }
        return 0;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance a = new Attendance(
                rs.getInt("attendance_id"),
                rs.getInt("student_id"),
                rs.getString("date"),
                rs.getString("entry_time"),
                rs.getString("exit_time"),
                rs.getString("status")
        );
        a.setStudentName(rs.getString("student_name"));
        return a;
    }
}
