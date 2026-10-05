package com.smarthostel.service;

import com.smarthostel.dao.RoomDAO;
import com.smarthostel.dao.StudentDAO;
import com.smarthostel.dao.UserDAO;
import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.RoomFullException;
import com.smarthostel.exception.StudentNotFoundException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Role;
import com.smarthostel.model.Room;
import com.smarthostel.model.Student;
import com.smarthostel.model.StudentRegistrationResult;
import com.smarthostel.model.User;
import com.smarthostel.util.PasswordGenerator;
import com.smarthostel.util.UsernameGenerator;
import com.smarthostel.util.Validator;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

/**
 * Service managing student lifecycle, room assignments, capacity checks,
 * transactional user account generation, password resets, and account status updates.
 */
public class StudentService {

    private final StudentDAO studentDAO;
    private final RoomDAO roomDAO;
    private final UserDAO userDAO;

    public StudentService() {
        this.studentDAO = new StudentDAO();
        this.roomDAO = new RoomDAO();
        this.userDAO = new UserDAO();
    }

    public StudentService(StudentDAO studentDAO, RoomDAO roomDAO) {
        this.studentDAO = studentDAO;
        this.roomDAO = roomDAO;
        this.userDAO = new UserDAO();
    }

    public StudentService(StudentDAO studentDAO, RoomDAO roomDAO, UserDAO userDAO) {
        this.studentDAO = studentDAO;
        this.roomDAO = roomDAO;
        this.userDAO = userDAO;
    }

    /**
     * Registers a new student and automatically creates their user login account inside
     * an atomic SQLite database transaction.
     */
    public StudentRegistrationResult registerStudentWithAccount(Student student)
            throws ValidationException, RoomFullException, DatabaseException {
        validateStudent(student);

        if (student.getRoomId() != null) {
            Validator.requireNonEmpty(student.getBedNumber(), "Bed Number");
            Room room = roomDAO.getRoomById(student.getRoomId());
            if (room == null) {
                throw new ValidationException("Selected room does not exist.");
            }
            if (room.isFull()) {
                throw new RoomFullException("No available beds in the selected room.");
            }
            if (studentDAO.isBedOccupied(room.getRoomId(), student.getBedNumber(), null)) {
                throw new ValidationException("Bed '" + student.getBedNumber() + "' is already occupied in room " + room.getRoomNumber() + ".");
            }
        }

        // Generate unique student username (e.g. Arun Kumar -> arun.kumar, arun.kumar1)
        String username = UsernameGenerator.generateUsername(student.getName(), userDAO);

        // Generate secure random temporary password (min 8 chars, uppercase, lowercase, digit, special)
        String tempPassword = PasswordGenerator.generateTemporaryPassword();

        // Perform transactional insertion
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DatabaseConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Insert student
            Student createdStudent = studentDAO.addStudent(conn, student);

            // 2. Insert user account linked to student_id with temporary password status
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(tempPassword); // Hashed via PasswordUtil
            newUser.setRole(Role.STUDENT);
            newUser.setLinkedStudentId(createdStudent.getStudentId());
            newUser.setForcePasswordChange(true);
            newUser.setStatus("ACTIVE");

            User createdUser = userDAO.create(conn, newUser);

            // 3. Update room occupancy if assigned
            if (createdStudent.getRoomId() != null) {
                roomDAO.updateOccupancy(conn, createdStudent.getRoomId(), 1);
            }

            conn.commit();
            return new StudentRegistrationResult(createdStudent, createdUser, tempPassword);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Transaction rollback error: " + rollbackEx.getMessage());
                }
            }
            if (e instanceof ValidationException ve) throw ve;
            if (e instanceof RoomFullException rfe) throw rfe;
            if (e instanceof DatabaseException de) throw de;
            throw new DatabaseException("Failed to register student and user account: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (Exception ignored) {}
            }
        }
    }

    public Student addStudent(Student student) throws ValidationException, RoomFullException, DatabaseException {
        return registerStudentWithAccount(student).getStudent();
    }

    public void updateStudent(Student student) throws ValidationException, RoomFullException, StudentNotFoundException, DatabaseException {
        validateStudent(student);

        Student existing = studentDAO.getStudentById(student.getStudentId());
        if (existing == null) {
            throw new StudentNotFoundException("Student with ID " + student.getStudentId() + " not found.");
        }

        Integer oldRoomId = existing.getRoomId();
        Integer newRoomId = student.getRoomId();

        boolean roomChanged = (oldRoomId == null && newRoomId != null) ||
                              (oldRoomId != null && !oldRoomId.equals(newRoomId));

        if (roomChanged && newRoomId != null) {
            Room newRoom = roomDAO.getRoomById(newRoomId);
            if (newRoom != null && newRoom.isFull()) {
                throw new RoomFullException("Target room " + newRoom.getRoomNumber() + " is already full.");
            }
            if (student.getBedNumber() != null && studentDAO.isBedOccupied(newRoomId, student.getBedNumber(), student.getStudentId())) {
                throw new ValidationException("Bed '" + student.getBedNumber() + "' is already occupied in target room.");
            }
        }

        studentDAO.updateStudent(student);

        if (roomChanged) {
            if (oldRoomId != null) {
                roomDAO.updateOccupancy(oldRoomId, -1);
            }
            if (newRoomId != null) {
                roomDAO.updateOccupancy(newRoomId, 1);
            }
        }
    }

    public void deleteStudent(int studentId) throws StudentNotFoundException, DatabaseException {
        Student existing = studentDAO.getStudentById(studentId);
        if (existing == null) {
            throw new StudentNotFoundException("Student with ID " + studentId + " not found.");
        }

        if (existing.getRoomId() != null) {
            roomDAO.updateOccupancy(existing.getRoomId(), -1);
        }

        User user = userDAO.findByStudentId(studentId);
        if (user != null) {
            userDAO.deleteUser(user.getId());
        }

        studentDAO.deleteStudent(studentId);
    }

    public void vacateStudent(int studentId) throws StudentNotFoundException, DatabaseException {
        Student existing = studentDAO.getStudentById(studentId);
        if (existing == null) {
            throw new StudentNotFoundException("Student with ID " + studentId + " not found.");
        }

        if (existing.getRoomId() != null) {
            int oldRoomId = existing.getRoomId();
            existing.setRoomId(null);
            existing.setBedNumber(null);
            studentDAO.updateStudent(existing);
            roomDAO.updateOccupancy(oldRoomId, -1);
        }
    }

    public List<Student> getAllStudents() throws DatabaseException {
        return studentDAO.getAllStudents();
    }

    public List<Student> getStudentsByBlock(int blockId) throws DatabaseException {
        return studentDAO.getStudentsByBlock(blockId);
    }

    public Student getStudentById(int studentId) throws DatabaseException {
        return studentDAO.getStudentById(studentId);
    }

    public List<Student> searchStudents(String query) throws DatabaseException {
        if (query == null || query.trim().isEmpty()) {
            return studentDAO.getAllStudents();
        }
        return studentDAO.searchStudents(query.trim());
    }

    public List<Student> getRoommates(int studentId) throws DatabaseException {
        Student student = studentDAO.getStudentById(studentId);
        if (student == null || student.getRoomId() == null) {
            return Collections.emptyList();
        }
        return studentDAO.getRoommates(student.getRoomId(), studentId);
    }

    /**
     * Resets a student's password by generating a new temporary password,
     * hashing it, and setting forcePasswordChange to true.
     */
    public String resetStudentPassword(int studentId) throws ValidationException, DatabaseException {
        User user = userDAO.findByStudentId(studentId);
        if (user == null) {
            throw new ValidationException("No user login account found for student ID " + studentId);
        }
        String newTempPassword = PasswordGenerator.generateTemporaryPassword();
        userDAO.updatePassword(user.getId(), newTempPassword, true);
        return newTempPassword;
    }

    /**
     * Updates account status to ACTIVE or INACTIVE.
     */
    public void updateAccountStatus(int studentId, String status) throws ValidationException, DatabaseException {
        User user = userDAO.findByStudentId(studentId);
        if (user == null) {
            throw new ValidationException("No user login account found for student ID " + studentId);
        }
        userDAO.updateStatus(user.getId(), status.toUpperCase());
    }

    private void validateStudent(Student s) throws ValidationException {
        Validator.requireNonEmpty(s.getName(), "Student Name");
        Validator.requireNonEmpty(s.getDepartment(), "Department");
        Validator.validateYear(s.getYear());
        Validator.validatePhone(s.getPhone());
        Validator.validateEmail(s.getEmail());
    }
}
