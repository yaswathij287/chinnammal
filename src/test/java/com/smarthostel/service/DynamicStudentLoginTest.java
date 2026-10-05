package com.smarthostel.service;

import com.smarthostel.dao.RoomDAO;
import com.smarthostel.dao.StudentDAO;
import com.smarthostel.dao.UserDAO;
import com.smarthostel.database.DatabaseInitializer;
import com.smarthostel.database.DatabaseSeeder;
import com.smarthostel.exception.HostelException;
import com.smarthostel.exception.InvalidLoginException;
import com.smarthostel.model.Role;
import com.smarthostel.model.Room;
import com.smarthostel.model.Student;
import com.smarthostel.model.StudentRegistrationResult;
import com.smarthostel.model.User;
import com.smarthostel.util.PasswordGenerator;
import com.smarthostel.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicStudentLoginTest {

    private static StudentService studentService;
    private static AuthService authService;
    private static UserDAO userDAO;
    private static StudentDAO studentDAO;
    private static RoomDAO roomDAO;

    @BeforeAll
    public static void setUp() {
        DatabaseInitializer.initializeDatabase();
        DatabaseSeeder.seedIfNeeded();
        studentService = new StudentService();
        authService = new AuthService();
        userDAO = new UserDAO();
        studentDAO = new StudentDAO();
        roomDAO = new RoomDAO();
    }

    @Test
    @DisplayName("Verify username generator creates clean lowercase names and handles collisions")
    public void testUsernameGeneration() throws Exception {
        String u1 = UsernameGenerator.generateUsername("Arun Kumar", userDAO);
        assertNotNull(u1);
        assertTrue(u1.startsWith("arun.kumar"));

        String u2 = UsernameGenerator.generateUsername("  Dr. Rajesh Kumar! #2 ", userDAO);
        assertNotNull(u2);
        assertFalse(u2.contains(" "));
        assertFalse(u2.contains("#"));
        assertFalse(u2.contains("!"));
    }

    @Test
    @DisplayName("Verify temporary password generator creates strong passwords with all required charsets")
    public void testPasswordGenerator() {
        for (int i = 0; i < 10; i++) {
            String tempPass = PasswordGenerator.generateTemporaryPassword();
            assertNotNull(tempPass);
            assertTrue(tempPass.length() >= 8, "Password must be at least 8 chars");
            assertTrue(tempPass.matches(".*[A-Z].*"), "Must contain uppercase");
            assertTrue(tempPass.matches(".*[a-z].*"), "Must contain lowercase");
            assertTrue(tempPass.matches(".*[0-9].*"), "Must contain number");
            assertTrue(tempPass.matches(".*[@#$%&*!].*"), "Must contain special char");
        }
    }

    @Test
    @DisplayName("Verify full dynamic student registration, transaction, first login, reset password, and status toggle")
    public void testDynamicStudentRegistrationAndLifecycleFlow() throws Exception {
        // 1. Find an available room
        List<Room> rooms = roomDAO.getAllRooms();
        Room availableRoom = null;
        for (Room r : rooms) {
            if ("AVAILABLE".equalsIgnoreCase(r.getStatus()) && !r.isFull()) {
                availableRoom = r;
                break;
            }
        }
        assertNotNull(availableRoom, "Must have an available room for test");
        int initialOccupied = availableRoom.getOccupied();

        // 2. Register new student
        Student student = new Student();
        student.setName("Kavitha Dynamic");
        student.setDepartment("Information Technology");
        student.setYear(3);
        student.setPhone("9876500888");
        student.setEmail("kavitha.dyn@example.com");
        student.setRoomId(availableRoom.getRoomId());
        student.setBedNumber("Bed Dyn " + System.currentTimeMillis() % 1000);

        StudentRegistrationResult result = studentService.registerStudentWithAccount(student);
        assertNotNull(result);
        assertNotNull(result.getStudent());
        assertTrue(result.getStudent().getStudentId() > 0);
        assertNotNull(result.getUser());
        assertNotNull(result.getTemporaryPassword());

        String generatedUsername = result.getUser().getUsername();
        String tempPassword = result.getTemporaryPassword();
        assertTrue(generatedUsername.startsWith("kavitha.dynamic"));

        // 3. Verify user account exists in SQLite and is linked to student
        User userInDb = userDAO.findByUsername(generatedUsername);
        assertNotNull(userInDb);
        assertEquals(Role.STUDENT, userInDb.getRole());
        assertEquals(result.getStudent().getStudentId(), userInDb.getLinkedStudentId());
        assertTrue(userInDb.isForcePasswordChange());
        assertEquals("ACTIVE", userInDb.getStatus());

        // 4. Test initial login using temporary password
        User loggedInUser = authService.login(generatedUsername, tempPassword);
        assertNotNull(loggedInUser);
        assertTrue(loggedInUser.isForcePasswordChange(), "Account must be flagged with force password change");

        // 5. Perform First Login password change
        String newPermanentPassword = "MySecure@Pass2026";
        authService.changePassword(loggedInUser.getId(), newPermanentPassword);

        // 6. Re-authenticate with new password
        User reLoggedInUser = authService.login(generatedUsername, newPermanentPassword);
        assertNotNull(reLoggedInUser);
        assertFalse(reLoggedInUser.isForcePasswordChange(), "Account must no longer be flagged with force password change");

        // 7. Old temporary password should now fail
        assertThrows(Exception.class, () -> {
            authService.login(generatedUsername, tempPassword);
        });

        // 8. Test Admin Reset Password
        String resetTempPassword = studentService.resetStudentPassword(result.getStudent().getStudentId());
        assertNotNull(resetTempPassword);
        User userAfterReset = userDAO.findByUsername(generatedUsername);
        assertTrue(userAfterReset.isForcePasswordChange());

        // Login with reset temporary password
        User resetLogin = authService.login(generatedUsername, resetTempPassword);
        assertNotNull(resetLogin);
        assertTrue(resetLogin.isForcePasswordChange());

        // 9. Test Account Status Deactivation
        studentService.updateAccountStatus(result.getStudent().getStudentId(), "INACTIVE");
        User deactivated = userDAO.findByUsername(generatedUsername);
        assertEquals("INACTIVE", deactivated.getStatus());
        assertFalse(deactivated.isActive());

        // Login should now fail because account is inactive
        assertThrows(InvalidLoginException.class, () -> {
            authService.login(generatedUsername, resetTempPassword);
        });

        // Reactivate account
        studentService.updateAccountStatus(result.getStudent().getStudentId(), "ACTIVE");
        User reactivated = userDAO.findByUsername(generatedUsername);
        assertTrue(reactivated.isActive());

        // Cleanup
        studentService.deleteStudent(result.getStudent().getStudentId());
    }

    @Test
    @DisplayName("Verify that demo accounts do NOT exist in the database")
    public void testDemoAccountsDoNotExist() throws Exception {
        assertFalse(userDAO.existsByUsername("admin"));
        assertFalse(userDAO.existsByUsername("warden"));
        assertFalse(userDAO.existsByUsername("student"));
    }
}
