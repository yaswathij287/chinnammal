package com.chinnammal.controller;

import com.chinnammal.config.AuthInterceptor;
import com.chinnammal.dto.ApiResponse;
import com.chinnammal.dto.ChangePasswordRequest;
import com.chinnammal.dto.InitialAdminSetupRequest;
import com.chinnammal.dto.LoginRequest;
import com.chinnammal.dto.UserSessionDTO;
import com.smarthostel.dao.StudentDAO;
import com.smarthostel.dao.UserDAO;
import com.smarthostel.dao.WardenDAO;
import com.smarthostel.exception.InvalidLoginException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Role;
import com.smarthostel.model.Student;
import com.smarthostel.model.User;
import com.smarthostel.model.Warden;
import com.smarthostel.service.AuthService;
import com.smarthostel.util.Validator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthService authService = new AuthService();
    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final WardenDAO wardenDAO = new WardenDAO();

    @GetMapping("/setup-required")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isSetupRequired() {
        try {
            boolean required = !userDAO.hasAnyAdmin();
            return ResponseEntity.ok(ApiResponse.success(Map.of("setupRequired", required)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check system setup status: " + e.getMessage()));
        }
    }

    @PostMapping("/initial-setup")
    public ResponseEntity<ApiResponse<String>> initialSetup(@RequestBody InitialAdminSetupRequest req) {
        if (req == null || req.getFullName() == null || req.getEmail() == null || req.getUsername() == null
                || req.getPassword() == null || req.getConfirmPassword() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("All fields (Full Name, Email, Username, Password, Confirm Password) are required."));
        }

        try {
            if (userDAO.hasAnyAdmin()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Administrator account already exists. Setup is locked."));
            }

            Validator.requireNonEmpty(req.getFullName(), "Full Name");
            Validator.requireNonEmpty(req.getEmail(), "Email");
            Validator.validateEmail(req.getEmail().trim());
            Validator.requireNonEmpty(req.getUsername(), "Username");

            if (userDAO.existsByUsername(req.getUsername().trim())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Username already taken. Please choose another."));
            }

            if (!req.getPassword().equals(req.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Passwords do not match."));
            }

            Validator.validatePasswordRequirements(req.getPassword());

            User admin = new User();
            admin.setUsername(req.getUsername().trim().toLowerCase());
            admin.setPassword(req.getPassword()); // Automatically hashed with PBKDF2 by UserDAO
            admin.setRole(Role.ADMIN);
            admin.setStatus("ACTIVE");
            admin.setForcePasswordChange(false);
            userDAO.create(admin);

            return ResponseEntity.ok(ApiResponse.success("Administrator account created successfully.", null));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Initial setup failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserSessionDTO>> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        if (req == null || req.getUsername() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username and password are required."));
        }

        try {
            User user = authService.login(req.getUsername().trim(), req.getPassword().trim());
            UserSessionDTO sessionDto = buildSessionDTO(user);

            HttpSession session = request.getSession(true);
            session.setAttribute(AuthInterceptor.SESSION_USER, sessionDto);

            return ResponseEntity.ok(ApiResponse.success("Login successful", sessionDto));
        } catch (InvalidLoginException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Authentication error: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserSessionDTO>> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
        }
        UserSessionDTO user = (UserSessionDTO) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<UserSessionDTO>> changePassword(@RequestBody ChangePasswordRequest req, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Session expired. Please log in again."));
        }
        UserSessionDTO currentUser = (UserSessionDTO) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Not authenticated"));
        }

        if (req == null || req.getNewPassword() == null || req.getConfirmPassword() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("New password and confirm password are required."));
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("New password and confirmation do not match."));
        }

        try {
            authService.changePassword(currentUser.getUserId(), req.getNewPassword());
            currentUser.setForcePasswordChange(false);
            session.setAttribute(AuthInterceptor.SESSION_USER, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully.", currentUser));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to change password: " + e.getMessage()));
        }
    }

    private UserSessionDTO buildSessionDTO(User user) {
        UserSessionDTO dto = new UserSessionDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setForcePasswordChange(user.isForcePasswordChange());
        dto.setStatus(user.getStatus());

        if (user.getRole() == Role.ADMIN) {
            dto.setDisplayName("System Administrator");
        } else if (user.getRole() == Role.STUDENT) {
            dto.setStudentId(user.getLinkedStudentId());
            if (user.getLinkedStudentId() != null) {
                try {
                    Student s = studentDAO.getStudentById(user.getLinkedStudentId());
                    if (s != null) {
                        dto.setDisplayName(s.getName());
                    }
                } catch (Exception ignored) {}
            }
            if (dto.getDisplayName() == null) {
                dto.setDisplayName(user.getUsername());
            }
        } else if (user.getRole() == Role.WARDEN) {
            dto.setWardenId(user.getLinkedWardenId());
            if (user.getLinkedWardenId() != null) {
                try {
                    Warden w = wardenDAO.getWardenById(user.getLinkedWardenId());
                    if (w != null) {
                        dto.setDisplayName(w.getName());
                        dto.setAssignedBlockId(w.getAssignedBlock());
                    }
                } catch (Exception ignored) {}
            }
            if (dto.getDisplayName() == null) {
                dto.setDisplayName("Hostel Warden");
            }
        }
        return dto;
    }
}
