package com.smarthostel.service;

import com.smarthostel.dao.StudentDAO;
import com.smarthostel.dao.UserDAO;
import com.smarthostel.dao.WardenDAO;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.InvalidLoginException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Role;
import com.smarthostel.model.Student;
import com.smarthostel.model.User;
import com.smarthostel.model.Warden;
import com.smarthostel.util.SessionManager;
import com.smarthostel.util.Validator;

/**
 * Authentication service handling database-driven login, hashed password verification,
 * account status enforcement, role validation, and password changes.
 * Contains ZERO hardcoded demo accounts.
 */
public class AuthService {

    private final UserDAO userDAO;
    private final WardenDAO wardenDAO;
    private final StudentDAO studentDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.wardenDAO = new WardenDAO();
        this.studentDAO = new StudentDAO();
    }

    public AuthService(UserDAO userDAO, WardenDAO wardenDAO, StudentDAO studentDAO) {
        this.userDAO = userDAO;
        this.wardenDAO = wardenDAO;
        this.studentDAO = studentDAO;
    }

    public User login(String username, String password) throws ValidationException, InvalidLoginException, DatabaseException {
        Validator.requireNonEmpty(username, "Username");
        Validator.requireNonEmpty(password, "Password");

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            throw new InvalidLoginException("Invalid username or password. Please try again.");
        }

        // Enforce account status check: Inactive users cannot log in
        if (!user.isActive()) {
            throw new InvalidLoginException("Your account is currently deactivated. Please contact the hostel administrator.");
        }

        // Verify PBKDF2 hashed password strictly against the database
        User authenticated = userDAO.authenticate(username.trim(), password);
        if (authenticated == null) {
            throw new InvalidLoginException("Invalid username or password. Please try again.");
        }

        Warden associatedWarden = null;
        Student associatedStudent = null;

        if (authenticated.getRole() == Role.WARDEN) {
            if (authenticated.getLinkedWardenId() != null) {
                associatedWarden = wardenDAO.getWardenById(authenticated.getLinkedWardenId());
            }
            if (associatedWarden == null) {
                // Fallback by email match if warden_id wasn't populated
                associatedWarden = wardenDAO.getWardenByEmail(authenticated.getUsername());
            }
        } else if (authenticated.getRole() == Role.STUDENT) {
            if (authenticated.getLinkedStudentId() != null) {
                associatedStudent = studentDAO.getStudentById(authenticated.getLinkedStudentId());
            }
            if (associatedStudent == null) {
                associatedStudent = studentDAO.getStudentByEmail(authenticated.getUsername());
            }
        }

        SessionManager.startSession(authenticated, associatedWarden, associatedStudent);
        return authenticated;
    }

    public void changePassword(int userId, String newPassword) throws ValidationException, DatabaseException {
        Validator.validatePasswordRequirements(newPassword);
        userDAO.updatePassword(userId, newPassword, false);

        User current = SessionManager.getCurrentUser();
        if (current != null && current.getId() == userId) {
            current.setPassword(newPassword);
            current.setForcePasswordChange(false);
        }
    }

    public void logout() {
        SessionManager.clearSession();
    }
}
