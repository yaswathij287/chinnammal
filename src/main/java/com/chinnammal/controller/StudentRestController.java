package com.chinnammal.controller;

import com.chinnammal.config.AuthInterceptor;
import com.chinnammal.dto.*;
import com.smarthostel.dao.BlockDAO;
import com.smarthostel.dao.RoomDAO;
import com.smarthostel.dao.StudentDAO;
import com.smarthostel.model.*;
import com.smarthostel.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentRestController {

    private final StudentService studentService = new StudentService();
    private final FeeService feeService = new FeeService();
    private final ComplaintService complaintService = new ComplaintService();
    private final LeaveService leaveService = new LeaveService();
    private final AttendanceService attendanceService = new AttendanceService();
    private final VisitorService visitorService = new VisitorService();
    private final RoomDAO roomDAO = new RoomDAO();
    private final BlockDAO blockDAO = new BlockDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(HttpServletRequest request) {
        Integer studentId = getStudentId(request);
        if (studentId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Student profile not linked to session."));
        }

        try {
            Student student = studentDAO.getStudentById(studentId);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Student profile record not found."));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("student", student);

            // Room and roommates
            Room room = null;
            Block block = null;
            List<Student> roommates = new ArrayList<>();
            if (student.getRoomId() != null) {
                room = roomDAO.getRoomById(student.getRoomId());
                if (room != null) {
                    block = blockDAO.getBlockById(room.getBlockId());
                }
                roommates = studentService.getRoommates(studentId);
            }
            data.put("room", room);
            data.put("block", block);
            data.put("roommates", roommates);

            // Fees
            List<Fee> fees = feeService.getFeesByStudent(studentId);
            Fee activeFee = fees.isEmpty() ? null : fees.get(0);
            data.put("fee", activeFee);

            // Complaints
            List<Complaint> complaints = complaintService.getComplaintsByStudent(studentId);
            data.put("complaints", complaints);

            // Leaves
            List<LeaveRequest> leaves = leaveService.getLeaveRequestsByStudent(studentId);
            data.put("leaves", leaves);

            // Attendance
            List<Attendance> attendance = attendanceService.getAttendanceByStudent(studentId);
            double attendancePct = attendanceService.getStudentAttendancePercentage(studentId);
            data.put("attendance", attendance);
            data.put("attendancePercentage", Math.round(attendancePct * 10.0) / 10.0);

            // Visitors
            List<Visitor> visitors = visitorService.getVisitorsByStudent(studentId);
            data.put("visitors", visitors);

            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load student dashboard: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Student>> getProfile(HttpServletRequest request) {
        Integer studentId = getStudentId(request);
        if (studentId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        try {
            Student student = studentDAO.getStudentById(studentId);
            return ResponseEntity.ok(ApiResponse.success(student));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/room")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRoomDetails(HttpServletRequest request) {
        Integer studentId = getStudentId(request);
        if (studentId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        try {
            Student s = studentDAO.getStudentById(studentId);
            Map<String, Object> map = new HashMap<>();
            if (s != null && s.getRoomId() != null) {
                Room r = roomDAO.getRoomById(s.getRoomId());
                map.put("room", r);
                map.put("bedNumber", s.getBedNumber());
                map.put("roommates", studentService.getRoommates(studentId));
                if (r != null) {
                    map.put("block", blockDAO.getBlockById(r.getBlockId()));
                }
            }
            return ResponseEntity.ok(ApiResponse.success(map));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/fees")
    public ResponseEntity<ApiResponse<List<Fee>>> getFees(HttpServletRequest request) {
        Integer studentId = getStudentId(request);
        if (studentId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        try {
            List<Fee> fees = feeService.getFeesByStudent(studentId);
            return ResponseEntity.ok(ApiResponse.success(fees));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<List<Complaint>>> getComplaints(HttpServletRequest request) {
        Integer studentId = getStudentId(request);
        if (studentId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        try {
            List<Complaint> list = complaintService.getComplaintsByStudent(studentId);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/complaints")
    public ResponseEntity<ApiResponse<Complaint>> submitComplaint(@RequestBody ComplaintCreateRequest req, HttpServletRequest request) {
        Integer studentId = getStudentId(request);
        if (studentId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));

        if (req == null || req.getCategory() == null || req.getDescription() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Category and description are required."));
        }

        try {
            Student s = studentDAO.getStudentById(studentId);
            Integer roomId = s != null ? s.getRoomId() : req.getRoomId();
            Complaint c = new Complaint(
                    studentId,
                    req.getCategory().trim(),
                    req.getDescription().trim(),
                    roomId,
                    req.getPriority() != null ? req.getPriority().trim() : "MEDIUM"
            );
            Complaint saved = complaintService.submitComplaint(c);
            return ResponseEntity.ok(ApiResponse.success("Complaint submitted successfully.", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/leaves")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaves(HttpServletRequest request) {
        Integer studentId = getStudentId(request);
        if (studentId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        try {
            List<LeaveRequest> list = leaveService.getLeaveRequestsByStudent(studentId);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/leaves")
    public ResponseEntity<ApiResponse<LeaveRequest>> applyLeave(@RequestBody LeaveCreateRequest req, HttpServletRequest request) {
        Integer studentId = getStudentId(request);
        if (studentId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));

        if (req == null || req.getFromDate() == null || req.getToDate() == null || req.getReason() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("From date, to date, and reason are required."));
        }

        try {
            LeaveRequest lr = new LeaveRequest(
                    studentId,
                    req.getFromDate().trim(),
                    req.getToDate().trim(),
                    req.getReason().trim()
            );
            LeaveRequest saved = leaveService.applyLeave(lr);
            return ResponseEntity.ok(ApiResponse.success("Leave request submitted successfully.", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendance(HttpServletRequest request) {
        Integer studentId = getStudentId(request);
        if (studentId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        try {
            List<Attendance> list = attendanceService.getAttendanceByStudent(studentId);
            double pct = attendanceService.getStudentAttendancePercentage(studentId);
            Map<String, Object> map = new HashMap<>();
            map.put("records", list);
            map.put("percentage", Math.round(pct * 10.0) / 10.0);
            return ResponseEntity.ok(ApiResponse.success(map));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/visitors")
    public ResponseEntity<ApiResponse<List<Visitor>>> getVisitors(HttpServletRequest request) {
        Integer studentId = getStudentId(request);
        if (studentId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        try {
            List<Visitor> list = visitorService.getVisitorsByStudent(studentId);
            return ResponseEntity.ok(ApiResponse.success(list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(e.getMessage()));
        }
    }

    private Integer getStudentId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        UserSessionDTO user = (UserSessionDTO) session.getAttribute(AuthInterceptor.SESSION_USER);
        return (user != null) ? user.getStudentId() : null;
    }
}
