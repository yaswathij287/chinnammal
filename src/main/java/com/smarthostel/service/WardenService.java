package com.smarthostel.service;

import com.smarthostel.dao.UserDAO;
import com.smarthostel.dao.WardenDAO;
import com.smarthostel.database.DatabaseConnection;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Role;
import com.smarthostel.model.User;
import com.smarthostel.model.Warden;
import com.smarthostel.model.WardenRegistrationResult;
import com.smarthostel.util.PasswordGenerator;
import com.smarthostel.util.UsernameGenerator;
import com.smarthostel.util.Validator;

import java.sql.Connection;
import java.util.List;

/**
 * Service managing warden lifecycle, assignments, and dynamic account registration.
 */
public class WardenService {

    private final WardenDAO wardenDAO;
    private final UserDAO userDAO;

    public WardenService() {
        this.wardenDAO = new WardenDAO();
        this.userDAO = new UserDAO();
    }

    public WardenService(WardenDAO wardenDAO) {
        this.wardenDAO = wardenDAO;
        this.userDAO = new UserDAO();
    }

    public WardenService(WardenDAO wardenDAO, UserDAO userDAO) {
        this.wardenDAO = wardenDAO;
        this.userDAO = userDAO;
    }

    /**
     * Registers a new warden and automatically creates their login account with
     * an auto-generated username and temporary password inside an atomic database transaction.
     */
    public WardenRegistrationResult registerWardenWithAccount(Warden warden) throws ValidationException, DatabaseException {
        validateWarden(warden);

        if (wardenDAO.existsByEmployeeId(warden.getEmployeeId())) {
            throw new ValidationException("Employee ID '" + warden.getEmployeeId() + "' is already registered.");
        }

        // Generate unique username based on warden's full name (e.g., Priya Kumar -> priya.kumar, priya.kumar1)
        String username = UsernameGenerator.generateUsername(warden.getName(), userDAO);

        // Generate secure random temporary password (min 8 chars, upper, lower, number, special)
        String tempPassword = PasswordGenerator.generateTemporaryPassword();

        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DatabaseConnection.getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 1. Insert warden record
            Warden createdWarden = wardenDAO.addWarden(conn, warden);

            // 2. Insert user account linked to warden_id
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(tempPassword); // Will be securely hashed by UserDAO / PasswordUtil
            newUser.setRole(Role.WARDEN);
            newUser.setLinkedWardenId(createdWarden.getWardenId());
            newUser.setForcePasswordChange(true);
            newUser.setStatus("ACTIVE");

            User createdUser = userDAO.create(conn, newUser);

            conn.commit();
            return new WardenRegistrationResult(createdWarden, createdUser, tempPassword);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Transaction rollback error: " + rollbackEx.getMessage());
                }
            }
            if (e instanceof ValidationException ve) throw ve;
            if (e instanceof DatabaseException de) throw de;
            throw new DatabaseException("Failed to register warden and user account: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (Exception ignored) {}
            }
        }
    }

    public Warden addWarden(Warden warden) throws ValidationException, DatabaseException {
        return registerWardenWithAccount(warden).getWarden();
    }

    public void updateWarden(Warden warden) throws ValidationException, DatabaseException {
        validateWarden(warden);
        wardenDAO.updateWarden(warden);
    }

    public void deleteWarden(int wardenId) throws DatabaseException {
        User user = userDAO.findByWardenId(wardenId);
        if (user != null) {
            userDAO.deleteUser(user.getId());
        }
        wardenDAO.deleteWarden(wardenId);
    }

    public void assignBlock(int wardenId, Integer blockId) throws DatabaseException {
        wardenDAO.assignBlock(wardenId, blockId);
    }

    public List<Warden> getAllWardens() throws DatabaseException {
        return wardenDAO.getAllWardens();
    }

    public Warden getWardenById(int wardenId) throws DatabaseException {
        return wardenDAO.getWardenById(wardenId);
    }

    public Warden getWardenByEmail(String email) throws DatabaseException {
        return wardenDAO.getWardenByEmail(email);
    }

    /**
     * Resets a warden's password by generating a new temporary password,
     * hashing it, and setting forcePasswordChange to true.
     */
    public String resetWardenPassword(int wardenId) throws ValidationException, DatabaseException {
        User user = userDAO.findByWardenId(wardenId);
        if (user == null) {
            throw new ValidationException("No user login account found for warden ID " + wardenId);
        }
        String newTempPassword = PasswordGenerator.generateTemporaryPassword();
        userDAO.updatePassword(user.getId(), newTempPassword, true);
        return newTempPassword;
    }

    /**
     * Updates account status to ACTIVE or INACTIVE.
     */
    public void updateAccountStatus(int wardenId, String status) throws ValidationException, DatabaseException {
        User user = userDAO.findByWardenId(wardenId);
        if (user == null) {
            throw new ValidationException("No user login account found for warden ID " + wardenId);
        }
        userDAO.updateStatus(user.getId(), status.toUpperCase());
    }

    private void validateWarden(Warden w) throws ValidationException {
        Validator.requireNonEmpty(w.getName(), "Warden Name");
        Validator.requireNonEmpty(w.getEmployeeId(), "Employee ID");
        Validator.requireNonEmpty(w.getDepartment(), "Department");
        Validator.validatePhone(w.getPhone());
        Validator.validateEmail(w.getEmail());
    }
}
