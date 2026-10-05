package com.chinnammal;

import com.chinnammal.config.AuthInterceptor;
import com.chinnammal.controller.*;
import com.chinnammal.dto.*;
import com.smarthostel.dao.UserDAO;
import com.smarthostel.database.DatabaseInitializer;
import com.smarthostel.database.DatabaseSeeder;
import com.smarthostel.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import com.smarthostel.database.DatabaseConnection;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChinnammalWebIntegrationTest {

    private static AuthRestController authController;
    private static AdminRestController adminController;
    private static WardenRestController wardenController;
    private static StudentRestController studentController;
    private static CommonRestController commonController;
    private static UserDAO userDAO;

    private static String adminUsername = "chinnammal.admin";
    private static String adminPassword = "Admin@Master2026";

    private static String wardenUsername;
    private static String wardenTempPassword;
    private static Integer wardenId;

    private static String studentUsername;
    private static String studentTempPassword;
    private static Integer studentId;

    @BeforeAll
    public static void setUp() {
        DatabaseInitializer.initializeDatabase();
        DatabaseSeeder.seedIfNeeded();

        // Clear any leftover admin users so initial setup is strictly tested
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE role = 'ADMIN';")) {
            pstmt.executeUpdate();
        } catch (Exception ignored) {}

        authController = new AuthRestController();
        adminController = new AdminRestController();
        wardenController = new WardenRestController();
        studentController = new StudentRestController();
        commonController = new CommonRestController();
        userDAO = new UserDAO();
    }

    private static HttpServletRequest createMockRequest(Map<String, Object> sessionStore) {
        HttpSession mockSession = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    if ("getAttribute".equals(method.getName())) {
                        return sessionStore.get(args[0]);
                    }
                    if ("setAttribute".equals(method.getName())) {
                        sessionStore.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("invalidate".equals(method.getName())) {
                        sessionStore.clear();
                        return null;
                    }
                    return null;
                }
        );

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getSession".equals(method.getName())) {
                        return mockSession;
                    }
                    return null;
                }
        );
    }

    @Test
    @Order(1)
    public void testNoDemoAccountsInDatabase() throws Exception {
        assertFalse(userDAO.existsByUsername("admin"), "Default 'admin' account must not exist");
        assertFalse(userDAO.existsByUsername("warden"), "Default 'warden' account must not exist");
        assertFalse(userDAO.existsByUsername("student"), "Default 'student' account must not exist");
    }

    @Test
    @Order(2)
    public void testInitialAdminSetup() throws Exception {
        // 1. If no admin exists, setup-required must return true
        if (!userDAO.hasAnyAdmin()) {
            ResponseEntity<ApiResponse<Map<String, Boolean>>> reqResp = authController.isSetupRequired();
            assertTrue(reqResp.getBody().getData().get("setupRequired"));

            // 2. Perform initial admin setup
            InitialAdminSetupRequest setupReq = new InitialAdminSetupRequest(
                    "Dr. K. Chinnammal", "admin@chinnammal.edu", adminUsername, adminPassword, adminPassword
            );
            ResponseEntity<ApiResponse<String>> setupResp = authController.initialSetup(setupReq);
            assertEquals(HttpStatus.OK, setupResp.getStatusCode());
            assertTrue(setupResp.getBody().isSuccess());
        }

        // 3. Now setup-required must return false
        ResponseEntity<ApiResponse<Map<String, Boolean>>> afterResp = authController.isSetupRequired();
        assertFalse(afterResp.getBody().getData().get("setupRequired"));
    }

    @Test
    @Order(3)
    public void testAdminLogin() {
        Map<String, Object> store = new HashMap<>();
        HttpServletRequest request = createMockRequest(store);
        LoginRequest req = new LoginRequest(adminUsername, adminPassword);

        ResponseEntity<ApiResponse<UserSessionDTO>> response = authController.login(req, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        UserSessionDTO user = response.getBody().getData();
        assertEquals(adminUsername, user.getUsername());
        assertEquals(Role.ADMIN, user.getRole());
        assertFalse(user.isForcePasswordChange());
        assertEquals("ACTIVE", user.getStatus());

        assertNotNull(store.get(AuthInterceptor.SESSION_USER));
    }

    @Test
    @Order(4)
    public void testAdminDashboardStats() {
        ResponseEntity<ApiResponse<Map<String, Object>>> statsResp = adminController.getDashboardStats();
        assertEquals(HttpStatus.OK, statsResp.getStatusCode());
        assertNotNull(statsResp.getBody());
        Map<String, Object> data = statsResp.getBody().getData();
        assertNotNull(data);
        assertTrue((int) data.get("totalRooms") > 0);
    }

    @Test
    @Order(5)
    public void testAdminRegisterDynamicWarden() {
        WardenCreateRequest req = new WardenCreateRequest();
        req.setName("Priya Kumar");
        req.setEmployeeId("EMP-WRD-" + (System.currentTimeMillis() % 10000));
        req.setDepartment("Student Welfare");
        req.setPhone("9841234567");
        req.setEmail("priya.kumar" + (System.currentTimeMillis() % 1000) + "@chinnammal.edu");
        req.setAssignedBlock(1);
        req.setAddress("Campus Quarters A-12");

        ResponseEntity<ApiResponse<WardenRegistrationResponse>> resp = adminController.registerWarden(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isSuccess());

        WardenRegistrationResponse data = resp.getBody().getData();
        assertNotNull(data.getWarden());
        assertNotNull(data.getUsername());
        assertNotNull(data.getTemporaryPassword());

        wardenId = data.getWarden().getWardenId();
        wardenUsername = data.getUsername();
        wardenTempPassword = data.getTemporaryPassword();

        assertTrue(wardenUsername.startsWith("priya.kumar"), "Username should be generated as priya.kumar*");
        assertTrue(wardenTempPassword.length() >= 8, "Password must be at least 8 characters");
    }

    @Test
    @Order(6)
    public void testWardenFirstLoginAndChangePassword() {
        assertNotNull(wardenUsername);
        assertNotNull(wardenTempPassword);

        // 1. First login with temporary password
        Map<String, Object> store = new HashMap<>();
        HttpServletRequest request = createMockRequest(store);
        LoginRequest req = new LoginRequest(wardenUsername, wardenTempPassword);

        ResponseEntity<ApiResponse<UserSessionDTO>> loginResp = authController.login(req, request);
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        assertTrue(loginResp.getBody().getData().isForcePasswordChange(), "Temporary password must be flagged");

        // 2. Change password
        ChangePasswordRequest changeReq = new ChangePasswordRequest();
        changeReq.setNewPassword("Warden@Permanent2026");
        changeReq.setConfirmPassword("Warden@Permanent2026");

        ResponseEntity<ApiResponse<UserSessionDTO>> changeResp = authController.changePassword(changeReq, request);
        assertEquals(HttpStatus.OK, changeResp.getStatusCode());
        assertFalse(changeResp.getBody().getData().isForcePasswordChange());

        // 3. Re-login with new password
        Map<String, Object> secondStore = new HashMap<>();
        HttpServletRequest secondReq = createMockRequest(secondStore);
        ResponseEntity<ApiResponse<UserSessionDTO>> permLogin = authController.login(
                new LoginRequest(wardenUsername, "Warden@Permanent2026"), secondReq
        );
        assertEquals(HttpStatus.OK, permLogin.getStatusCode());
        assertFalse(permLogin.getBody().getData().isForcePasswordChange());
    }

    @Test
    @Order(7)
    public void testAdminRegisterDynamicStudent() {
        ResponseEntity<ApiResponse<java.util.List<com.smarthostel.model.Room>>> roomsResp = commonController.getAvailableRooms();
        assertNotNull(roomsResp.getBody());
        assertFalse(roomsResp.getBody().getData().isEmpty());
        int availRoomId = roomsResp.getBody().getData().get(0).getRoomId();

        StudentCreateRequest createReq = new StudentCreateRequest();
        createReq.setName("Suresh Raina");
        createReq.setDepartment("Computer Science");
        createReq.setYear(2);
        createReq.setPhone("9840123789");
        createReq.setEmail("suresh.raina" + (System.currentTimeMillis() % 1000) + "@example.com");
        createReq.setRoomId(availRoomId);
        createReq.setBedNumber("Bed-Tst-" + (System.currentTimeMillis() % 1000));

        ResponseEntity<ApiResponse<StudentRegistrationResponse>> regResp = adminController.registerStudent(createReq);
        assertEquals(HttpStatus.OK, regResp.getStatusCode());
        assertNotNull(regResp.getBody());

        StudentRegistrationResponse regData = regResp.getBody().getData();
        assertNotNull(regData.getStudent());
        assertNotNull(regData.getUsername());
        assertNotNull(regData.getTemporaryPassword());

        studentId = regData.getStudent().getStudentId();
        studentUsername = regData.getUsername();
        studentTempPassword = regData.getTemporaryPassword();

        assertTrue(studentUsername.startsWith("suresh.raina"));
        assertTrue(studentTempPassword.length() >= 8);
    }

    @Test
    @Order(8)
    public void testStudentFirstLoginAndChangePassword() {
        // 1. Login with temporary password
        Map<String, Object> store = new HashMap<>();
        HttpServletRequest request = createMockRequest(store);
        LoginRequest req = new LoginRequest(studentUsername, studentTempPassword);

        ResponseEntity<ApiResponse<UserSessionDTO>> loginResp = authController.login(req, request);
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        assertTrue(loginResp.getBody().getData().isForcePasswordChange(), "Temporary password must be flagged");

        // 2. Change password
        ChangePasswordRequest changeReq = new ChangePasswordRequest();
        changeReq.setNewPassword("Student@Permanent2026");
        changeReq.setConfirmPassword("Student@Permanent2026");

        ResponseEntity<ApiResponse<UserSessionDTO>> changeResp = authController.changePassword(changeReq, request);
        assertEquals(HttpStatus.OK, changeResp.getStatusCode());
        assertFalse(changeResp.getBody().getData().isForcePasswordChange());

        // 3. Re-login with new password
        Map<String, Object> secondStore = new HashMap<>();
        HttpServletRequest secondReq = createMockRequest(secondStore);
        ResponseEntity<ApiResponse<UserSessionDTO>> permLogin = authController.login(
                new LoginRequest(studentUsername, "Student@Permanent2026"), secondReq
        );
        assertEquals(HttpStatus.OK, permLogin.getStatusCode());
        assertFalse(permLogin.getBody().getData().isForcePasswordChange());
    }

    @Test
    @Order(9)
    public void testAdminResetPasswordForWarden() {
        // Admin resets warden password
        ResponseEntity<ApiResponse<PasswordResetResponse>> resetResp = adminController.resetWardenPassword(wardenId);
        assertEquals(HttpStatus.OK, resetResp.getStatusCode());
        assertNotNull(resetResp.getBody());

        String newTempPassword = resetResp.getBody().getData().getTemporaryPassword();
        assertNotNull(newTempPassword);

        // Login with new temporary password
        Map<String, Object> store = new HashMap<>();
        HttpServletRequest req = createMockRequest(store);
        ResponseEntity<ApiResponse<UserSessionDTO>> loginResp = authController.login(
                new LoginRequest(wardenUsername, newTempPassword), req
        );
        assertEquals(HttpStatus.OK, loginResp.getStatusCode());
        assertTrue(loginResp.getBody().getData().isForcePasswordChange(), "Reset password must require change");
    }

    @Test
    @Order(10)
    public void testAccountStatusDeactivationBlocksLogin() {
        // Deactivate warden account
        ResponseEntity<ApiResponse<String>> deactResp = adminController.updateWardenStatus(
                wardenId, Map.of("status", "INACTIVE")
        );
        assertEquals(HttpStatus.OK, deactResp.getStatusCode());

        // Attempt login -> must be rejected with 401 Unauthorized
        Map<String, Object> store = new HashMap<>();
        HttpServletRequest req = createMockRequest(store);
        ResponseEntity<ApiResponse<UserSessionDTO>> loginResp = authController.login(
                new LoginRequest(wardenUsername, "Warden@Permanent2026"), req
        );
        assertEquals(HttpStatus.UNAUTHORIZED, loginResp.getStatusCode());
        assertTrue(loginResp.getBody().getMessage().contains("deactivated"));

        // Reactivate warden account
        ResponseEntity<ApiResponse<String>> reactResp = adminController.updateWardenStatus(
                wardenId, Map.of("status", "ACTIVE")
        );
        assertEquals(HttpStatus.OK, reactResp.getStatusCode());
    }

    @Test
    @Order(11)
    public void testStudentDashboardAndComplaint() {
        Map<String, Object> store = new HashMap<>();
        HttpServletRequest request = createMockRequest(store);
        authController.login(new LoginRequest(studentUsername, "Student@Permanent2026"), request);

        ResponseEntity<ApiResponse<Map<String, Object>>> dashResp = studentController.getDashboard(request);
        assertEquals(HttpStatus.OK, dashResp.getStatusCode());
        assertNotNull(dashResp.getBody().getData().get("student"));

        ComplaintCreateRequest compReq = new ComplaintCreateRequest();
        compReq.setCategory("Electrical");
        compReq.setDescription("Ceiling fan regulator replacement needed");
        compReq.setPriority("MEDIUM");

        ResponseEntity<ApiResponse<com.smarthostel.model.Complaint>> compResp = studentController.submitComplaint(compReq, request);
        assertEquals(HttpStatus.OK, compResp.getStatusCode());
        assertTrue(compResp.getBody().isSuccess());
    }

    @AfterAll
    public static void tearDown() {
        // Remove test admin so the database is left ready for the user to create their own admin account
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE role = 'ADMIN';")) {
            pstmt.executeUpdate();
        } catch (Exception ignored) {}
    }
}
