package com.chinnammal.controller;

import com.chinnammal.config.AuthInterceptor;
import com.chinnammal.dto.*;
import com.smarthostel.model.*;
import com.smarthostel.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warden")
public class WardenRestController {

    private final StudentService studentService = new StudentService();
    private final RoomService roomService = new RoomService();
    private final ComplaintService complaintService = new ComplaintService();
    private final LeaveService leaveService = new LeaveService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final VisitorService visitorService = new VisitorService();
    private final SecurityService securityService = new SecurityService();

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWardenStats(HttpServletRequest request) {
        UserSessionDTO user = getSessionUser(request);
        Integer blockId = user != null ? user.getAssignedBlockId() : null;

        try {
            Map<String, Object> stats = new HashMap<>();
            List<Student> students = (blockId != null) ? studentService.getStudentsByBlock(blockId) : studentService.getAllStudents();
            List<Room> rooms = (blockId != null) ? roomService.getRoomsByBlock(blockId) : roomService.getAllRooms();
            List<Complaint> complaints = (blockId != null) ? complaintService.getComplaintsByBlock(blockId) : complaintService.getAllComplaints();
            List<LeaveRequest> leaves = (blockId != null) ? leaveService.getLeaveRequestsByBlock(blockId) : leaveService.getAllLeaveRequests();

            long openComplaints = complaints.stream().filter(c -> "OPEN".equalsIgnoreCase(c.getStatus()) || "PENDING".equalsIgnoreCase(c.getStatus())).count();
            long pendingLeaves = leaves.stream().filter(l -> "PENDING".equalsIgnoreCase(l.getStatus())).count();

            stats.put("totalStudents", students.size());
            stats.put("totalRooms", rooms.size());
            stats.put("openComplaints", openComplaints);
            stats.put("pendingLeaves", pendingLeaves);
            stats.put("assignedBlockId", blockId);

            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Failed to load warden stats: " + e.getMessage()));
        }
    }

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<Student>>> getBlockStudents(HttpServletRequest request) {
        UserSessionDTO user = getSessionUser(request);
        Integer blockId = user != null ? user.getAssignedBlockId() : null;
        try {
            List<Student> list = (blockId != null) ? studentService.getStudentsByBlock(blockId) : studentService.getAllStudents();
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<Room>>> getBlockRooms(HttpServletRequest request) {
        UserSessionDTO user = getSessionUser(request);
        Integer blockId = user != null ? user.getAssignedBlockId() : null;
        try {
            List<Room> list = (blockId != null) ? roomService.getRoomsByBlock(blockId) : roomService.getAllRooms();
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<List<Complaint>>> getComplaints(HttpServletRequest request) {
        UserSessionDTO user = getSessionUser(request);
        Integer blockId = user != null ? user.getAssignedBlockId() : null;
        try {
            List<Complaint> list = (blockId != null) ? complaintService.getComplaintsByBlock(blockId) : complaintService.getAllComplaints();
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
            return ResponseEntity.ok(ApiResponse.success("Complaint updated to " + status, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/leaves")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaveRequests(HttpServletRequest request) {
        UserSessionDTO user = getSessionUser(request);
        Integer blockId = user != null ? user.getAssignedBlockId() : null;
        try {
            List<LeaveRequest> list = (blockId != null) ? leaveService.getLeaveRequestsByBlock(blockId) : leaveService.getAllLeaveRequests();
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/leaves/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateLeaveStatus(@PathVariable("id") int id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        UserSessionDTO user = getSessionUser(request);
        int wardenId = (user != null && user.getWardenId() != null) ? user.getWardenId() : 1;
        String status = body != null ? body.get("status") : null;
        if (status == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status is required."));
        }
        try {
            if ("APPROVED".equalsIgnoreCase(status.trim())) {
                leaveService.approveLeave(id, wardenId);
            } else {
                leaveService.rejectLeave(id, wardenId);
            }
            return ResponseEntity.ok(ApiResponse.success("Leave marked as " + status.toUpperCase(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendance() {
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
            return ResponseEntity.ok(ApiResponse.success("Attendance recorded.", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/visitors")
    public ResponseEntity<ApiResponse<List<Visitor>>> getVisitors(HttpServletRequest request) {
        UserSessionDTO user = getSessionUser(request);
        Integer blockId = user != null ? user.getAssignedBlockId() : null;
        try {
            List<Visitor> list = (blockId != null) ? visitorService.getVisitorsByBlock(blockId) : visitorService.getAllVisitors();
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
            return ResponseEntity.ok(ApiResponse.success("Visitor pass created.", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/security-logs")
    public ResponseEntity<ApiResponse<List<SecurityLog>>> getSecurityLogs() {
        try {
            List<SecurityLog> list = securityService.getRecentLogs(30);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    private UserSessionDTO getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (UserSessionDTO) session.getAttribute(AuthInterceptor.SESSION_USER) : null;
    }
}
