package com.smarthostel.service;

import com.smarthostel.dao.UserDAO;
import com.smarthostel.database.DatabaseInitializer;
import com.smarthostel.database.DatabaseSeeder;
import com.smarthostel.exception.InvalidLoginException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Role;
import com.smarthostel.model.User;
import com.smarthostel.util.SessionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private static AuthService authService;
    private static UserDAO userDAO;

    @AfterAll
    public static void tearDown() {
        try {
            User a = userDAO.findByUsername("test.admin");
            if (a != null) userDAO.deleteUser(a.getId());
            User in = userDAO.findByUsername("test.inactive");
            if (in != null) userDAO.deleteUser(in.getId());
        } catch (Exception ignored) {}
    }

    @BeforeAll
    public static void setup() throws Exception {
        DatabaseInitializer.initializeDatabase();
        DatabaseSeeder.seedIfNeeded();
        authService = new AuthService();
        userDAO = new UserDAO();

        // Ensure a test admin user exists dynamically
        if (!userDAO.existsByUsername("test.admin")) {
            User admin = new User();
            admin.setUsername("test.admin");
            admin.setPassword("AdminSecure@123");
            admin.setRole(Role.ADMIN);
            admin.setStatus("ACTIVE");
            admin.setForcePasswordChange(false);
            userDAO.create(admin);
        }

        // Ensure a test inactive user exists
        if (!userDAO.existsByUsername("test.inactive")) {
            User inactive = new User();
            inactive.setUsername("test.inactive");
            inactive.setPassword("Inactive@123");
            inactive.setRole(Role.STUDENT);
            inactive.setStatus("INACTIVE");
            inactive.setForcePasswordChange(false);
            userDAO.create(inactive);
        }
    }

    @Test
    @DisplayName("Verify that hardcoded demo accounts do NOT exist in the database")
    public void testDemoAccountsDoNotExist() throws Exception {
        assertFalse(userDAO.existsByUsername("admin"), "Demo 'admin' account must not exist");
        assertFalse(userDAO.existsByUsername("warden"), "Demo 'warden' account must not exist");
        assertFalse(userDAO.existsByUsername("student"), "Demo 'student' account must not exist");
    }

    @Test
    @DisplayName("Admin user login authentication with hashed password")
    public void testAdminLogin() throws Exception {
        User user = authService.login("test.admin", "AdminSecure@123");
        assertNotNull(user);
        assertEquals("test.admin", user.getUsername());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(Role.ADMIN, SessionManager.getCurrentRole());
    }

    @Test
    @DisplayName("Inactive account authentication should be rejected")
    public void testInactiveAccountLogin() {
        assertThrows(InvalidLoginException.class, () -> {
            authService.login("test.inactive", "Inactive@123");
        });
    }

    @Test
    @DisplayName("Invalid credentials should throw InvalidLoginException")
    public void testInvalidPassword() {
        assertThrows(InvalidLoginException.class, () -> {
            authService.login("test.admin", "wrong_password");
        });
    }

    @Test
    @DisplayName("Blank credentials should throw ValidationException")
    public void testEmptyCredentials() {
        assertThrows(ValidationException.class, () -> {
            authService.login("", "AdminSecure@123");
        });
    }
}
