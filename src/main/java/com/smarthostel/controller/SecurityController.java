package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.model.SecurityLog;
import com.smarthostel.service.FaceDetectionService;
import com.smarthostel.service.SecurityService;

import java.util.List;

/**
 * Controller mediating security operations, logging, and face verification.
 */
public class SecurityController {

    private final SecurityService securityService;
    private final FaceDetectionService faceDetectionService;

    public SecurityController() {
        this.securityService = new SecurityService();
        this.faceDetectionService = new FaceDetectionService();
    }

    public SecurityController(SecurityService securityService, FaceDetectionService faceDetectionService) {
        this.securityService = securityService;
        this.faceDetectionService = faceDetectionService;
    }

    public List<SecurityLog> getAllLogs() throws DatabaseException {
        return securityService.getAllLogs();
    }

    public List<SecurityLog> getRecentLogs(int limit) throws DatabaseException {
        return securityService.getRecentLogs(limit);
    }

    public SecurityLog logManualEvent(Integer studentId, String eventType, String gate, String method) throws DatabaseException {
        return securityService.logEvent(studentId, eventType, gate, method);
    }

    public FaceDetectionService.VerificationResult performFaceVerification(int studentId, String gate, String eventType) throws DatabaseException {
        return faceDetectionService.verifyStudentFace(studentId, gate, eventType);
    }

    public int getSecurityAlertsCount() throws DatabaseException {
        return securityService.getSecurityAlertsCount();
    }
}
