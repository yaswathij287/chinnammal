package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.Warden;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for wardens table.
 * Supports employeeId, department, address, and joined user account credentials.
 */
public class WardenDAO {

    public List<Warden> getAllWardens() throws DatabaseException {
        List<Warden> list = new ArrayList<>();
        String sql = """
            SELECT w.warden_id, w.name, w.employee_id, w.department, w.phone, w.email,
                   w.assigned_block, w.address, w.created_at, b.block_name,
                   u.username, u.status AS user_status, u.id AS user_id
            FROM wardens w
            LEFT JOIN blocks b ON w.assigned_block = b.block_id
            LEFT JOIN users u ON u.linked_warden_id = w.warden_id
            ORDER BY w.name;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToWarden(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve wardens: " + e.getMessage(), e);
        }
        return list;
    }

    public Warden getWardenById(int wardenId) throws DatabaseException {
        String sql = """
            SELECT w.warden_id, w.name, w.employee_id, w.department, w.phone, w.email,
                   w.assigned_block, w.address, w.created_at, b.block_name,
                   u.username, u.status AS user_status, u.id AS user_id
            FROM wardens w
            LEFT JOIN blocks b ON w.assigned_block = b.block_id
            LEFT JOIN users u ON u.linked_warden_id = w.warden_id
            WHERE w.warden_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, wardenId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToWarden(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve warden: " + e.getMessage(), e);
        }
        return null;
    }

    public Warden getWardenByEmail(String email) throws DatabaseException {
        if (email == null) return null;
        String sql = """
            SELECT w.warden_id, w.name, w.employee_id, w.department, w.phone, w.email,
                   w.assigned_block, w.address, w.created_at, b.block_name,
                   u.username, u.status AS user_status, u.id AS user_id
            FROM wardens w
            LEFT JOIN blocks b ON w.assigned_block = b.block_id
            LEFT JOIN users u ON u.linked_warden_id = w.warden_id
            WHERE LOWER(w.email) = LOWER(?);
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email.trim());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToWarden(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve warden by email: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean existsByEmployeeId(String employeeId) throws DatabaseException {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM wardens WHERE LOWER(employee_id) = LOWER(?);";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, employeeId.trim());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check if employee ID exists: " + e.getMessage(), e);
        }
        return false;
    }

    public Warden addWarden(Warden warden) throws DatabaseException {
        try {
            Connection conn = DatabaseConnection.getConnection();
            return addWarden(conn, warden);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to obtain database connection: " + e.getMessage(), e);
        }
    }

    public Warden addWarden(Connection conn, Warden warden) throws DatabaseException {
        String sql = """
            INSERT INTO wardens (name, employee_id, department, phone, email, assigned_block, address)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, warden.getName().trim());
            pstmt.setString(2, warden.getEmployeeId() != null ? warden.getEmployeeId().trim() : null);
            pstmt.setString(3, warden.getDepartment() != null ? warden.getDepartment().trim() : null);
            pstmt.setString(4, warden.getPhone().trim());
            pstmt.setString(5, warden.getEmail().trim());

            if (warden.getAssignedBlock() != null) {
                pstmt.setInt(6, warden.getAssignedBlock());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setString(7, warden.getAddress() != null ? warden.getAddress().trim() : null);

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    warden.setWardenId(generatedKeys.getInt(1));
                }
            }
            return warden;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add warden: " + e.getMessage(), e);
        }
    }

    public void updateWarden(Warden warden) throws DatabaseException {
        String sql = """
            UPDATE wardens
            SET name = ?, employee_id = ?, department = ?, phone = ?, email = ?, assigned_block = ?, address = ?
            WHERE warden_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, warden.getName().trim());
                pstmt.setString(2, warden.getEmployeeId() != null ? warden.getEmployeeId().trim() : null);
                pstmt.setString(3, warden.getDepartment() != null ? warden.getDepartment().trim() : null);
                pstmt.setString(4, warden.getPhone().trim());
                pstmt.setString(5, warden.getEmail().trim());

                if (warden.getAssignedBlock() != null) {
                    pstmt.setInt(6, warden.getAssignedBlock());
                } else {
                    pstmt.setNull(6, Types.INTEGER);
                }

                pstmt.setString(7, warden.getAddress() != null ? warden.getAddress().trim() : null);
                pstmt.setInt(8, warden.getWardenId());

                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update warden: " + e.getMessage(), e);
        }
    }

    public void deleteWarden(int wardenId) throws DatabaseException {
        String sql = "DELETE FROM wardens WHERE warden_id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, wardenId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete warden: " + e.getMessage(), e);
        }
    }

    public void assignBlock(int wardenId, Integer blockId) throws DatabaseException {
        String sql = "UPDATE wardens SET assigned_block = ? WHERE warden_id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (blockId != null) {
                    pstmt.setInt(1, blockId);
                } else {
                    pstmt.setNull(1, Types.INTEGER);
                }
                pstmt.setInt(2, wardenId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to assign block to warden: " + e.getMessage(), e);
        }
    }

    public int count() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM wardens;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count wardens: " + e.getMessage(), e);
        }
        return 0;
    }

    private Warden mapResultSetToWarden(ResultSet rs) throws SQLException {
        Integer blockId = (Integer) rs.getObject("assigned_block");
        Warden w = new Warden();
        w.setWardenId(rs.getInt("warden_id"));
        w.setName(rs.getString("name"));
        w.setEmployeeId(rs.getString("employee_id"));
        w.setDepartment(rs.getString("department"));
        w.setPhone(rs.getString("phone"));
        w.setEmail(rs.getString("email"));
        w.setAssignedBlock(blockId);
        w.setAddress(rs.getString("address"));
        w.setCreatedAt(rs.getTimestamp("created_at"));
        w.setBlockName(rs.getString("block_name"));

        // User account details from join
        try {
            w.setUsername(rs.getString("username"));
            String status = rs.getString("user_status");
            w.setStatus(status != null ? status : "ACTIVE");
            w.setUserId((Integer) rs.getObject("user_id"));
        } catch (SQLException ignored) {}

        return w;
    }
}
