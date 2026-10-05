package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.Role;
import com.smarthostel.model.User;
import com.smarthostel.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 * Data Access Object for users table.
 * Supports hashed credentials, dynamic student/warden linkage, account status, and password change flags.
 */
public class UserDAO {

    public User findByUsername(String username) throws DatabaseException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String sql = """
            SELECT id, username, password_hash, role, linked_student_id, linked_warden_id,
                   force_password_change, status, created_at, updated_at
            FROM users
            WHERE LOWER(username) = LOWER(?);
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username.trim());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by username: " + e.getMessage(), e);
        }
        return null;
    }

    public User findById(int id) throws DatabaseException {
        String sql = """
            SELECT id, username, password_hash, role, linked_student_id, linked_warden_id,
                   force_password_change, status, created_at, updated_at
            FROM users
            WHERE id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by id: " + e.getMessage(), e);
        }
        return null;
    }

    public User findByStudentId(int studentId) throws DatabaseException {
        String sql = """
            SELECT id, username, password_hash, role, linked_student_id, linked_warden_id,
                   force_password_change, status, created_at, updated_at
            FROM users
            WHERE linked_student_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by student ID: " + e.getMessage(), e);
        }
        return null;
    }

    public User findByWardenId(int wardenId) throws DatabaseException {
        String sql = """
            SELECT id, username, password_hash, role, linked_student_id, linked_warden_id,
                   force_password_change, status, created_at, updated_at
            FROM users
            WHERE linked_warden_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, wardenId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by warden ID: " + e.getMessage(), e);
        }
        return null;
    }

    public User authenticate(String username, String rawPassword) throws DatabaseException {
        User user = findByUsername(username);
        if (user == null) {
            return null;
        }

        // Verify PBKDF2 hashed password
        if (PasswordUtil.verifyPassword(rawPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public User create(User user) throws DatabaseException {
        try {
            Connection conn = DatabaseConnection.getConnection();
            return create(conn, user);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to obtain database connection: " + e.getMessage(), e);
        }
    }

    public User create(Connection conn, User user) throws DatabaseException {
        String rawOrHashed = user.getPasswordHash();
        String finalHash;
        if (rawOrHashed != null && rawOrHashed.contains(":")) {
            finalHash = rawOrHashed;
        } else {
            finalHash = PasswordUtil.hashPassword(rawOrHashed);
        }
        user.setPasswordHash(finalHash);

        String sql = """
            INSERT INTO users (username, password, password_hash, role, linked_student_id, linked_warden_id,
                               force_password_change, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername().trim().toLowerCase());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getRole().name());

            if (user.getLinkedStudentId() != null) {
                pstmt.setInt(5, user.getLinkedStudentId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            if (user.getLinkedWardenId() != null) {
                pstmt.setInt(6, user.getLinkedWardenId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setInt(7, user.isForcePasswordChange() ? 1 : 0);
            pstmt.setString(8, user.getStatus() != null ? user.getStatus() : "ACTIVE");

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create user: " + e.getMessage(), e);
        }
    }

    public void updatePassword(int userId, String newPassword, boolean forcePasswordChange) throws DatabaseException {
        String finalHash = newPassword.contains(":") ? newPassword : PasswordUtil.hashPassword(newPassword);
        String sql = "UPDATE users SET password = ?, password_hash = ?, force_password_change = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, finalHash);
                pstmt.setString(2, finalHash);
                pstmt.setInt(3, forcePasswordChange ? 1 : 0);
                pstmt.setInt(4, userId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update password: " + e.getMessage(), e);
        }
    }

    public void updateStatus(int userId, String status) throws DatabaseException {
        String sql = "UPDATE users SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status.toUpperCase());
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user status: " + e.getMessage(), e);
        }
    }

    public boolean existsByUsername(String username) throws DatabaseException {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?);";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username.trim());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check if username exists: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean hasAnyAdmin() throws DatabaseException {
        String sql = "SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check if any admin exists: " + e.getMessage(), e);
        }
    }

    public int count() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM users;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count users: " + e.getMessage(), e);
        }
        return 0;
    }

    public void deleteUser(int userId) throws DatabaseException {
        String sql = "DELETE FROM users WHERE id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Integer studentId = (Integer) rs.getObject("linked_student_id");
        Integer wardenId = (Integer) rs.getObject("linked_warden_id");

        String hash = null;
        try {
            hash = rs.getString("password_hash");
        } catch (SQLException ignored) {}
        if (hash == null || hash.isBlank()) {
            try {
                hash = rs.getString("password");
            } catch (SQLException ignored) {}
        }

        User u = new User(
                rs.getInt("id"),
                rs.getString("username"),
                hash,
                Role.fromString(rs.getString("role")),
                studentId,
                wardenId,
                rs.getInt("force_password_change") == 1,
                rs.getString("status"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
        return u;
    }
}
