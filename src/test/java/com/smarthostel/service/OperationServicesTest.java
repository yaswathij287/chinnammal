package com.smarthostel.service;

import com.smarthostel.database.DatabaseInitializer;
import com.smarthostel.database.DatabaseSeeder;
import com.smarthostel.model.Complaint;
import com.smarthostel.model.LeaveRequest;
import com.smarthostel.model.SecurityLog;
import com.smarthostel.model.Visitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OperationServicesTest {

    private static ComplaintService complaintService;
    private static VisitorService visitorService;
    private static LeaveService leaveService;
    private static SecurityService securityService;
    private static FaceDetectionService faceDetectionService;

    @BeforeAll
    public static void setup() {
        DatabaseInitializer.initializeDatabase();
        DatabaseSeeder.seedIfNeeded();
        complaintService = new ComplaintService();
        visitorService = new VisitorService();
        leaveService = new LeaveService();
        securityService = new SecurityService();
        faceDetectionService = new FaceDetectionService();
    }

    @Test
    @DisplayName("Verify Complaint lifecycle: submit, update, resolve")
    public void testComplaintWorkflow() throws Exception {
        Complaint c = new Complaint(1, "Electrical", "Ceiling fan speed regulator broken", 1, "HIGH");
        Complaint created = complaintService.submitComplaint(c);
        assertNotNull(created);
        assertEquals("OPEN", created.getStatus());

        complaintService.updateStatus(created.getComplaintId(), "IN_PROGRESS");
        complaintService.updateStatus(created.getComplaintId(), "RESOLVED");

        List<Complaint> list = complaintService.getAllComplaints();
        Complaint found = list.stream().filter(item -> item.getComplaintId() == created.getComplaintId()).findFirst().orElse(null);
        assertNotNull(found);
        assertEquals("RESOLVED", found.getStatus());
    }

    @Test
    @DisplayName("Verify Visitor pass request and approval")
    public void testVisitorWorkflow() throws Exception {
        Visitor v = new Visitor(1, "Suresh Kumar", "Uncle", "9876543210", LocalDate.now().toString(), "04:00 PM");
        Visitor created = visitorService.requestVisitor(v);
        assertNotNull(created);
        assertEquals("PENDING", created.getStatus());

        visitorService.approveVisitor(created.getVisitorId());
        List<Visitor> visitors = visitorService.getAllVisitors();
        Visitor found = visitors.stream().filter(item -> item.getVisitorId() == created.getVisitorId()).findFirst().orElse(null);
        assertNotNull(found);
        assertEquals("APPROVED", found.getStatus());
    }

    @Test
    @DisplayName("Verify Leave application and approval")
    public void testLeaveWorkflow() throws Exception {
        String from = LocalDate.now().plusDays(5).toString();
        String to = LocalDate.now().plusDays(7).toString();
        LeaveRequest req = new LeaveRequest(1, from, to, "Attending technical conference");
        LeaveRequest created = leaveService.applyLeave(req);
        assertNotNull(created);
        assertEquals("PENDING", created.getStatus());

        com.smarthostel.dao.WardenDAO wardenDAO = new com.smarthostel.dao.WardenDAO();
        List<com.smarthostel.model.Warden> wardens = wardenDAO.getAllWardens();
        int wardenId;
        if (wardens.isEmpty()) {
            com.smarthostel.model.Warden w = new com.smarthostel.model.Warden("Test Warden", "9876543210", "warden.test@example.com", 1);
            wardenDAO.addWarden(w);
            wardenId = w.getWardenId();
        } else {
            wardenId = wardens.get(0).getWardenId();
        }

        leaveService.approveLeave(created.getLeaveId(), wardenId);
        List<LeaveRequest> leaves = leaveService.getAllLeaveRequests();
        LeaveRequest found = leaves.stream().filter(item -> item.getLeaveId() == created.getLeaveId()).findFirst().orElse(null);
        assertNotNull(found);
        assertEquals("APPROVED", found.getStatus());
    }

    @Test
    @DisplayName("Verify Security Logging and Face Detection Service")
    public void testSecurityAndFaceVerification() throws Exception {
        SecurityLog log = securityService.logEvent(1, "ENTRY", "Main Gate", "CARD");
        assertNotNull(log);
        assertTrue(log.getLogId() > 0);

        var faceResult = faceDetectionService.verifyStudentFace(1, "Gate 1", "ENTRY");
        assertNotNull(faceResult);
        assertTrue(faceResult.isSuccess());
        assertTrue(faceResult.getConfidence() >= 90.0);
        assertNotNull(faceResult.getSecurityLog());
    }
}
