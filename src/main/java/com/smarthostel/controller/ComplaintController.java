package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Complaint;
import com.smarthostel.service.ComplaintService;

import java.util.List;

/**
 * Controller mediating complaints and resolutions.
 */
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController() {
        this.complaintService = new ComplaintService();
    }

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    public List<Complaint> getAllComplaints() throws DatabaseException {
        return complaintService.getAllComplaints();
    }

    public List<Complaint> getComplaintsByBlock(int blockId) throws DatabaseException {
        return complaintService.getComplaintsByBlock(blockId);
    }

    public List<Complaint> getComplaintsByStudent(int studentId) throws DatabaseException {
        return complaintService.getComplaintsByStudent(studentId);
    }

    public Complaint submitComplaint(Complaint complaint) throws ValidationException, DatabaseException {
        return complaintService.submitComplaint(complaint);
    }

    public void updateStatus(int complaintId, String status) throws ValidationException, DatabaseException {
        complaintService.updateStatus(complaintId, status);
    }

    public int getPendingComplaintsCount() throws DatabaseException {
        return complaintService.getPendingComplaintsCount();
    }
}
