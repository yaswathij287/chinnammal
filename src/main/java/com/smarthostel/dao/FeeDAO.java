package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.Fee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for fees table.
 */
public class FeeDAO {

    public List<Fee> getAllFees() throws DatabaseException {
        List<Fee> list = new ArrayList<>();
        String sql = """
            SELECT f.fee_id, f.student_id, f.total_amount, f.paid_amount, f.balance, f.due_date, f.status,
                   s.name AS student_name
            FROM fees f
            JOIN students s ON f.student_id = s.student_id
            ORDER BY f.due_date ASC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToFee(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve fees: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Fee> getFeesByStudent(int studentId) throws DatabaseException {
        List<Fee> list = new ArrayList<>();
        String sql = """
            SELECT f.fee_id, f.student_id, f.total_amount, f.paid_amount, f.balance, f.due_date, f.status,
                   s.name AS student_name
            FROM fees f
            JOIN students s ON f.student_id = s.student_id
            WHERE f.student_id = ?
            ORDER BY f.due_date DESC;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToFee(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve student fees: " + e.getMessage(), e);
        }
        return list;
    }

    public Fee getFeeById(int feeId) throws DatabaseException {
        String sql = """
            SELECT f.fee_id, f.student_id, f.total_amount, f.paid_amount, f.balance, f.due_date, f.status,
                   s.name AS student_name
            FROM fees f
            JOIN students s ON f.student_id = s.student_id
            WHERE f.fee_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, feeId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToFee(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve fee by id: " + e.getMessage(), e);
        }
        return null;
    }

    public Fee addFee(Fee fee) throws DatabaseException {
        String sql = """
            INSERT INTO fees (student_id, total_amount, paid_amount, balance, due_date, status)
            VALUES (?, ?, ?, ?, ?, ?);
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, fee.getStudentId());
                pstmt.setDouble(2, fee.getTotalAmount());
                pstmt.setDouble(3, fee.getPaidAmount());
                pstmt.setDouble(4, fee.getBalance());
                pstmt.setString(5, fee.getDueDate());
                pstmt.setString(6, fee.getStatus());
                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        fee.setFeeId(generatedKeys.getInt(1));
                    }
                }
                return fee;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add fee record: " + e.getMessage(), e);
        }
    }

    public void recordPayment(int feeId, double amount) throws DatabaseException {
        String sql = """
            UPDATE fees
            SET paid_amount = paid_amount + ?,
                balance = total_amount - (paid_amount + ?),
                status = CASE
                    WHEN (total_amount - (paid_amount + ?)) <= 0 THEN 'PAID'
                    ELSE 'PENDING'
                END
            WHERE fee_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, amount);
                pstmt.setDouble(2, amount);
                pstmt.setDouble(3, amount);
                pstmt.setInt(4, feeId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to record fee payment: " + e.getMessage(), e);
        }
    }

    public double getTotalPendingFees() throws DatabaseException {
        String sql = "SELECT COALESCE(SUM(balance), 0) FROM fees WHERE status != 'PAID';";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate pending fees: " + e.getMessage(), e);
        }
        return 0.0;
    }

    public double getTotalCollectedFees() throws DatabaseException {
        String sql = "SELECT COALESCE(SUM(paid_amount), 0) FROM fees;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate collected fees: " + e.getMessage(), e);
        }
        return 0.0;
    }

    private Fee mapResultSetToFee(ResultSet rs) throws SQLException {
        Fee f = new Fee(
                rs.getInt("fee_id"),
                rs.getInt("student_id"),
                rs.getDouble("total_amount"),
                rs.getDouble("paid_amount"),
                rs.getDouble("balance"),
                rs.getString("due_date"),
                rs.getString("status")
        );
        f.setStudentName(rs.getString("student_name"));
        return f;
    }
}
