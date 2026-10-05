package com.chinnammal.controller;

import com.chinnammal.dto.*;
import com.smarthostel.dao.BlockDAO;
import com.smarthostel.dao.RoomDAO;
import com.smarthostel.dao.SecurityLogDAO;
import com.smarthostel.dao.UserDAO;
import com.smarthostel.model.*;
import com.smarthostel.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final StudentService studentService = new StudentService();
    private final WardenService wardenService = new WardenService();
    private final RoomService roomService = new RoomService();
    private final FeeService feeService = new FeeService();
    private final ComplaintService complaintService = new ComplaintService();
    private final LeaveService leaveService = new LeaveService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final VisitorService visitorService = new VisitorService();
    private final SecurityService securityService = new SecurityService();
    private final ReportService reportService = new ReportService();

    private final BlockDAO blockDAO = new BlockDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final SecurityLogDAO securityLogDAO = new SecurityLogDAO();
    private final UserDAO userDAO = new UserDAO();

    // ==========================================
    // DASHBOARD & STATS
    // ==========================================
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        try {
            Map<String, Object> stats = reportService.getDashboardMetrics();
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load dashboard statistics: " + e.getMessage()));
        }
    }

    // ==========================================
    // STUDENTS MANAGEMENT
    // ==========================================
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents(@RequestParam(value = "search", required = false) String search) {
        try {
            List<Student> list;
            if (search != null && !search.trim().isEmpty()) {
                list = studentService.searchStudents(search.trim());
            } else {
                list = studentService.getAllStudents();
            }
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch students: " + e.getMessage()));
        }
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<StudentRegistrationResponse>> registerStudent(@RequestBody StudentCreateRequest req) {
        if (req == null || req.getName() == null || req.getDepartment() == null || req.getPhone() == null || req.getEmail() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Name, department, phone, and email are required."));
        }

        try {
            Student s = new Student(
                    req.getName().trim(),
                    req.getDepartment().trim(),
                    req.getYear(),
                    req.getPhone().trim(),
                    req.getEmail().trim(),
                    req.getRoomId(),
                    req.getBedNumber() != null ? req.getBedNumber().trim() : null
            );

            StudentRegistrationResult result = studentService.registerStudentWithAccount(s);
            StudentRegistrationResponse resp = new StudentRegistrationResponse(
                    result.getStudent(),
                    result.getUser().getUsername(),
                    result.getTemporaryPassword()
            );

            return ResponseEntity.ok(ApiResponse.success("Student registered successfully with dynamic login credentials", resp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<ApiResponse<String>> updateStudent(@PathVariable("id") int id, @RequestBody StudentUpdateRequest req) {
        try {
            Student s = new Student(
                    req.getName().trim(),
                    req.getDepartment().trim(),
                    req.getYear(),
                    req.getPhone().trim(),
                    req.getEmail().trim(),
                    req.getRoomId(),
                    req.getBedNumber() != null ? req.getBedNumber().trim() : null
            );
            s.setStudentId(id);
            studentService.updateStudent(s);
            return ResponseEntity.ok(ApiResponse.success("Student details updated successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse<String>> deleteStudent(@PathVariable("id") int id) {
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.ok(ApiResponse.success("Student record deleted successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/students/{id}/reset-password")
    public ResponseEntity<ApiResponse<PasswordResetResponse>> resetStudentPassword(@PathVariable("id") int id) {
        try {
            Student s = studentService.getStudentById(id);
            if (s == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Student not found."));
            }
            User u = userDAO.findByStudentId(id);
            if (u == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No user account associated with this student."));
            }

            String tempPassword = studentService.resetStudentPassword(id);
            PasswordResetResponse resp = new PasswordResetResponse(u.getUsername(), tempPassword);
            return ResponseEntity.ok(ApiResponse.success("Temporary password generated successfully.", resp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/students/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateStudentStatus(@PathVariable("id") int id, @RequestBody Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        if (status == null || (!"ACTIVE".equalsIgnoreCase(status.trim()) && !"INACTIVE".equalsIgnoreCase(status.trim()))) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Valid status required ('ACTIVE' or 'INACTIVE')."));
        }
        try {
            studentService.updateAccountStatus(id, status.trim().toUpperCase());
            return ResponseEntity.ok(ApiResponse.success("Student account status changed to " + status.toUpperCase(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // WARDENS MANAGEMENT
    // ==========================================
    @GetMapping("/wardens")
    public ResponseEntity<ApiResponse<List<Warden>>> getAllWardens() {
        try {
            List<Warden> list = wardenService.getAllWardens();
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch wardens: " + e.getMessage()));
        }
    }

    @PostMapping("/wardens")
    public ResponseEntity<ApiResponse<WardenRegistrationResponse>> registerWarden(@RequestBody WardenCreateRequest req) {
        if (req == null || req.getName() == null || req.getEmployeeId() == null || req.getDepartment() == null
                || req.getPhone() == null || req.getEmail() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Full Name, Employee ID, Department, Phone, and Email are required."));
        }
        try {
            Warden w = new Warden(
                    req.getName().trim(),
                    req.getEmployeeId().trim(),
                    req.getDepartment().trim(),
                    req.getPhone().trim(),
                    req.getEmail().trim(),
                    req.getAssignedBlock(),
                    req.getAddress() != null ? req.getAddress().trim() : null
            );

            WardenRegistrationResult result = wardenService.registerWardenWithAccount(w);
            WardenRegistrationResponse resp = new WardenRegistrationResponse(
                    result.getWarden(),
                    result.getUser().getUsername(),
                    result.getTemporaryPassword()
            );

            return ResponseEntity.ok(ApiResponse.success("Warden registered successfully with dynamic login credentials", resp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/wardens/{id}")
    public ResponseEntity<ApiResponse<String>> updateWarden(@PathVariable("id") int id, @RequestBody WardenCreateRequest req) {
        try {
            Warden w = new Warden(
                    req.getName().trim(),
                    req.getEmployeeId() != null ? req.getEmployeeId().trim() : null,
                    req.getDepartment() != null ? req.getDepartment().trim() : null,
                    req.getPhone().trim(),
                    req.getEmail().trim(),
                    req.getAssignedBlock(),
                    req.getAddress() != null ? req.getAddress().trim() : null
            );
            w.setWardenId(id);
            wardenService.updateWarden(w);
            return ResponseEntity.ok(ApiResponse.success("Warden details updated successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/wardens/{id}")
    public ResponseEntity<ApiResponse<String>> deleteWarden(@PathVariable("id") int id) {
        try {
            wardenService.deleteWarden(id);
            return ResponseEntity.ok(ApiResponse.success("Warden deleted successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/wardens/{id}/reset-password")
    public ResponseEntity<ApiResponse<PasswordResetResponse>> resetWardenPassword(@PathVariable("id") int id) {
        try {
            Warden w = wardenService.getWardenById(id);
            if (w == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Warden not found."));
            }
            User u = userDAO.findByWardenId(id);
            if (u == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("No user account associated with this warden."));
            }

            String tempPassword = wardenService.resetWardenPassword(id);
            PasswordResetResponse resp = new PasswordResetResponse(u.getUsername(), tempPassword);
            return ResponseEntity.ok(ApiResponse.success("Temporary password generated successfully.", resp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/wardens/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateWardenStatus(@PathVariable("id") int id, @RequestBody Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        if (status == null || (!"ACTIVE".equalsIgnoreCase(status.trim()) && !"INACTIVE".equalsIgnoreCase(status.trim()))) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Valid status required ('ACTIVE' or 'INACTIVE')."));
        }
        try {
            wardenService.updateAccountStatus(id, status.trim().toUpperCase());
            return ResponseEntity.ok(ApiResponse.success("Warden account status changed to " + status.toUpperCase(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // BLOCKS & ROOMS
    // ==========================================
    @GetMapping("/blocks")
    public ResponseEntity<ApiResponse<List<Block>>> getAllBlocks() {
        try {
            List<Block> blocks = blockDAO.getAllBlocks();
            return ResponseEntity.ok(ApiResponse.success(blocks));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<Room>>> getAllRooms(@RequestParam(value = "blockId", required = false) Integer blockId) {
        try {
            List<Room> rooms;
            if (blockId != null) {
                rooms = roomService.getRoomsByBlock(blockId);
            } else {
                rooms = roomService.getAllRooms();
            }
            return ResponseEntity.ok(ApiResponse.success(rooms));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<Room>> addRoom(@RequestBody RoomCreateRequest req) {
        if (req == null || req.getRoomNumber() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Room number and block are required."));
        }
        try {
            Room r = new Room(req.getRoomNumber().trim(), req.getBlockId(), req.getCapacity());
            Room added = roomService.addRoom(r);
            return ResponseEntity.ok(ApiResponse.success("Room created successfully.", added));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // FEES MANAGEMENT
    // ==========================================
    @GetMapping("/fees")
    public ResponseEntity<ApiResponse<List<Fee>>> getAllFees() {
        try {
            List<Fee> fees = feeService.getAllFees();
            return ResponseEntity.ok(ApiResponse.success(fees));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/fees/pay")
    public ResponseEntity<ApiResponse<String>> recordFeePayment(@RequestBody FeePaymentRequest req) {
        if (req == null || req.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Valid payment amount required."));
        }
        try {
            feeService.recordPayment(req.getFeeId(), req.getAmount());
            return ResponseEntity.ok(ApiResponse.success("Fee payment recorded successfully.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // COMPLAINTS
    // ==========================================
    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<List<Complaint>>> getAllComplaints() {
        try {
            List<Complaint> list = complaintService.getAllComplaints();
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/complaints/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateComplaintStatus(@PathVariable("id") int id, @RequestBody Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status is required."));
        }
        try {
            complaintService.updateStatus(id, status.trim());
            return ResponseEntity.ok(ApiResponse.success("Complaint status updated to " + status, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // LEAVE REQUESTS
    // ==========================================
    @GetMapping("/leaves")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getAllLeaves() {
        try {
            List<LeaveRequest> list = leaveService.getAllLeaveRequests();
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/leaves/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateLeaveStatus(@PathVariable("id") int id, @RequestBody Map<String, String> body) {
        String status = body != null ? body.get("status") : null;
        if (status == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status is required (APPROVED or REJECTED)."));
        }
        try {
            if ("APPROVED".equalsIgnoreCase(status.trim())) {
                leaveService.approveLeave(id, 1);
            } else {
                leaveService.rejectLeave(id, 1);
            }
            return ResponseEntity.ok(ApiResponse.success("Leave request marked as " + status.toUpperCase(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // ATTENDANCE
    // ==========================================
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendance(@RequestParam(value = "date", required = false) String date) {
        try {
            List<Attendance> list = attendanceService.getAllAttendance();
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/attendance")
    public ResponseEntity<ApiResponse<Attendance>> recordAttendance(@RequestBody AttendanceRecordRequest req) {
        if (req == null || req.getStatus() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Attendance details required."));
        }
        try {
            String date = req.getDate() != null && !req.getDate().isEmpty() ? req.getDate() : LocalDate.now().toString();
            Attendance att = new Attendance(req.getStudentId(), date, "09:00 AM", null, req.getStatus().toUpperCase());
            Attendance saved = attendanceService.markAttendance(att);
            return ResponseEntity.ok(ApiResponse.success("Attendance marked successfully.", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // VISITORS
    // ==========================================
    @GetMapping("/visitors")
    public ResponseEntity<ApiResponse<List<Visitor>>> getAllVisitors() {
        try {
            List<Visitor> list = visitorService.getAllVisitors();
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/visitors")
    public ResponseEntity<ApiResponse<Visitor>> recordVisitor(@RequestBody VisitorRecordRequest req) {
        if (req == null || req.getVisitorName() == null || req.getPhone() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Visitor name and phone are required."));
        }
        try {
            String visitDate = req.getVisitDate() != null && !req.getVisitDate().isEmpty() ? req.getVisitDate() : LocalDate.now().toString();
            Visitor v = new Visitor(req.getStudentId(), req.getVisitorName().trim(), req.getRelationship().trim(), req.getPhone().trim(), visitDate, "10:00 AM");
            v.setStatus("APPROVED");
            Visitor created = visitorService.requestVisitor(v);
            return ResponseEntity.ok(ApiResponse.success("Visitor entry pass issued.", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/visitors/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateVisitorStatus(@PathVariable("id") int id, @RequestBody Map<String, String> body) {
        String status = body != null ? body.get("status") : "CHECKED_OUT";
        try {
            visitorService.approveVisitor(id);
            return ResponseEntity.ok(ApiResponse.success("Visitor status updated.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // SECURITY LOGS
    // ==========================================
    @GetMapping("/security-logs")
    public ResponseEntity<ApiResponse<List<SecurityLog>>> getSecurityLogs(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        try {
            List<SecurityLog> list = securityService.getRecentLogs(limit);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/security-logs")
    public ResponseEntity<ApiResponse<SecurityLog>> createSecurityLog(@RequestBody Map<String, Object> req) {
        try {
            Integer studentId = req.get("studentId") != null ? Integer.valueOf(req.get("studentId").toString()) : null;
            String eventType = req.get("eventType") != null ? req.get("eventType").toString() : "GATE_ENTRY";
            String gate = req.get("gate") != null ? req.get("gate").toString() : "MAIN_GATE";
            String verificationMethod = req.get("verificationMethod") != null ? req.get("verificationMethod").toString() : "BIOMETRIC";

            SecurityLog log = securityService.logEvent(studentId, eventType, gate, verificationMethod);
            return ResponseEntity.ok(ApiResponse.success("Security log recorded.", log));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==========================================
    // REPORTS
    // ==========================================
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReports() {
        try {
            Map<String, Object> metrics = reportService.getDashboardMetrics();
            String reportText = reportService.generateSummaryReportText();
            // Rebrand header in text
            reportText = reportText.replace("SMART HOSTEL MANAGEMENT SYSTEM", "CHINNAMMAL — SMART MANAGEMENT PLATFORM");
            metrics.put("reportText", reportText);
            return ResponseEntity.ok(ApiResponse.success(metrics));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }
}
