package com.smarthostel.dao;

import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for students table.
 * Supports joined user account credentials and active status.
 */
public class StudentDAO {

    public List<Student> getAllStudents() throws DatabaseException {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.student_id, s.name, s.department, s.year, s.phone, s.email, s.room_id, s.bed_number, s.created_at,
                   r.room_number, b.block_name,
                   u.username, u.status AS user_status, u.id AS user_id
            FROM students s
            LEFT JOIN rooms r ON s.room_id = r.room_id
            LEFT JOIN blocks b ON r.block_id = b.block_id
            LEFT JOIN users u ON u.linked_student_id = s.student_id
            ORDER BY s.name;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve students: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Student> getStudentsByBlock(int blockId) throws DatabaseException {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.student_id, s.name, s.department, s.year, s.phone, s.email, s.room_id, s.bed_number, s.created_at,
                   r.room_number, b.block_name,
                   u.username, u.status AS user_status, u.id AS user_id
            FROM students s
            JOIN rooms r ON s.room_id = r.room_id
            JOIN blocks b ON r.block_id = b.block_id
            LEFT JOIN users u ON u.linked_student_id = s.student_id
            WHERE b.block_id = ?
            ORDER BY s.name;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, blockId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToStudent(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve students by block: " + e.getMessage(), e);
        }
        return list;
    }

    public Student getStudentById(int studentId) throws DatabaseException {
        String sql = """
            SELECT s.student_id, s.name, s.department, s.year, s.phone, s.email, s.room_id, s.bed_number, s.created_at,
                   r.room_number, b.block_name,
                   u.username, u.status AS user_status, u.id AS user_id
            FROM students s
            LEFT JOIN rooms r ON s.room_id = r.room_id
            LEFT JOIN blocks b ON r.block_id = b.block_id
            LEFT JOIN users u ON u.linked_student_id = s.student_id
            WHERE s.student_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToStudent(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve student: " + e.getMessage(), e);
        }
        return null;
    }

    public Student getStudentByEmail(String email) throws DatabaseException {
        String sql = """
            SELECT s.student_id, s.name, s.department, s.year, s.phone, s.email, s.room_id, s.bed_number, s.created_at,
                   r.room_number, b.block_name,
                   u.username, u.status AS user_status, u.id AS user_id
            FROM students s
            LEFT JOIN rooms r ON s.room_id = r.room_id
            LEFT JOIN blocks b ON r.block_id = b.block_id
            LEFT JOIN users u ON u.linked_student_id = s.student_id
            WHERE LOWER(s.email) = LOWER(?);
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToStudent(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve student by email: " + e.getMessage(), e);
        }
        return null;
    }

    public Student addStudent(Student student) throws DatabaseException {
        try {
            Connection conn = DatabaseConnection.getConnection();
            return addStudent(conn, student);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to obtain connection: " + e.getMessage(), e);
        }
    }

    public Student addStudent(Connection conn, Student student) throws DatabaseException {
        String sql = """
            INSERT INTO students (name, department, year, phone, email, room_id, bed_number)
            VALUES (?, ?, ?, ?, ?, ?, ?);
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getDepartment());
            pstmt.setInt(3, student.getYear());
            pstmt.setString(4, student.getPhone());
            pstmt.setString(5, student.getEmail());
            if (student.getRoomId() != null) {
                pstmt.setInt(6, student.getRoomId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            pstmt.setString(7, student.getBedNumber());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    student.setStudentId(generatedKeys.getInt(1));
                }
            }
            return student;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add student: " + e.getMessage(), e);
        }
    }

    public boolean isBedOccupied(int roomId, String bedNumber, Integer excludeStudentId) throws DatabaseException {
        if (bedNumber == null || bedNumber.trim().isEmpty()) {
            return false;
        }
        String sql = """
            SELECT COUNT(*) FROM students
            WHERE room_id = ?
              AND LOWER(TRIM(bed_number)) = LOWER(TRIM(?))
              AND (student_id != ? OR ? IS NULL);
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, roomId);
                pstmt.setString(2, bedNumber.trim());
                if (excludeStudentId != null) {
                    pstmt.setInt(3, excludeStudentId);
                    pstmt.setInt(4, excludeStudentId);
                } else {
                    pstmt.setInt(3, -1);
                    pstmt.setNull(4, Types.INTEGER);
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check bed occupancy: " + e.getMessage(), e);
        }
        return false;
    }

    public void updateStudent(Student student) throws DatabaseException {
        String sql = """
            UPDATE students
            SET name = ?, department = ?, year = ?, phone = ?, email = ?, room_id = ?, bed_number = ?
            WHERE student_id = ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, student.getName());
                pstmt.setString(2, student.getDepartment());
                pstmt.setInt(3, student.getYear());
                pstmt.setString(4, student.getPhone());
                pstmt.setString(5, student.getEmail());
                if (student.getRoomId() != null) {
                    pstmt.setInt(6, student.getRoomId());
                } else {
                    pstmt.setNull(6, Types.INTEGER);
                }
                pstmt.setString(7, student.getBedNumber());
                pstmt.setInt(8, student.getStudentId());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update student: " + e.getMessage(), e);
        }
    }

    public void deleteStudent(int studentId) throws DatabaseException {
        String sql = "DELETE FROM students WHERE student_id = ?;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, studentId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete student: " + e.getMessage(), e);
        }
    }

    public List<Student> getRoommates(int roomId, int excludeStudentId) throws DatabaseException {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.student_id, s.name, s.department, s.year, s.phone, s.email, s.room_id, s.bed_number, s.created_at,
                   r.room_number, b.block_name,
                   u.username, u.status AS user_status, u.id AS user_id
            FROM students s
            JOIN rooms r ON s.room_id = r.room_id
            JOIN blocks b ON r.block_id = b.block_id
            LEFT JOIN users u ON u.linked_student_id = s.student_id
            WHERE s.room_id = ? AND s.student_id != ?;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, roomId);
                pstmt.setInt(2, excludeStudentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToStudent(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get roommates: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Student> searchStudents(String query) throws DatabaseException {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.student_id, s.name, s.department, s.year, s.phone, s.email, s.room_id, s.bed_number, s.created_at,
                   r.room_number, b.block_name,
                   u.username, u.status AS user_status, u.id AS user_id
            FROM students s
            LEFT JOIN rooms r ON s.room_id = r.room_id
            LEFT JOIN blocks b ON r.block_id = b.block_id
            LEFT JOIN users u ON u.linked_student_id = s.student_id
            WHERE s.name LIKE ? OR s.department LIKE ? OR s.phone LIKE ? OR s.email LIKE ?
            ORDER BY s.name;
        """;
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String pattern = "%" + query + "%";
                pstmt.setString(1, pattern);
                pstmt.setString(2, pattern);
                pstmt.setString(3, pattern);
                pstmt.setString(4, pattern);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToStudent(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search students: " + e.getMessage(), e);
        }
        return list;
    }

    public int count() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM students;";
        try {
            Connection conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count students: " + e.getMessage(), e);
        }
        return 0;
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Integer roomId = (Integer) rs.getObject("room_id");
        Student s = new Student(
                rs.getInt("student_id"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getInt("year"),
                rs.getString("phone"),
                rs.getString("email"),
                roomId,
                rs.getString("bed_number"),
                rs.getTimestamp("created_at")
        );
        s.setRoomNumber(rs.getString("room_number"));
        s.setBlockName(rs.getString("block_name"));

        // User account details from join
        try {
            s.setUsername(rs.getString("username"));
            String status = rs.getString("user_status");
            s.setStatus(status != null ? status : "ACTIVE");
            s.setUserId((Integer) rs.getObject("user_id"));
        } catch (SQLException ignored) {}

        return s;
    }
}
