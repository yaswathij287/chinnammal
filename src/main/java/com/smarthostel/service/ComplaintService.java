package com.smarthostel.service;

import com.smarthostel.dao.ComplaintDAO;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Complaint;
import com.smarthostel.util.Validator;

import java.util.List;

/**
 * Service managing student grievance submissions, status transitions, and resolution.
 */
public class ComplaintService {

    private final ComplaintDAO complaintDAO;

    public ComplaintService() {
        this.complaintDAO = new ComplaintDAO();
    }

    public ComplaintService(ComplaintDAO complaintDAO) {
        this.complaintDAO = complaintDAO;
    }

    public Complaint submitComplaint(Complaint complaint) throws ValidationException, DatabaseException {
        Validator.requireNonEmpty(complaint.getCategory(), "Category");
        Validator.requireNonEmpty(complaint.getDescription(), "Description");
        if (complaint.getPriority() == null || complaint.getPriority().trim().isEmpty()) {
            complaint.setPriority("MEDIUM");
        }
        complaint.setStatus("OPEN");
        return complaintDAO.addComplaint(complaint);
    }

    public void updateStatus(int complaintId, String status) throws ValidationException, DatabaseException {
        Validator.requireNonEmpty(status, "Status");
        complaintDAO.updateStatus(complaintId, status.toUpperCase());
    }

    public List<Complaint> getAllComplaints() throws DatabaseException {
        return complaintDAO.getAllComplaints();
    }

    public List<Complaint> getComplaintsByBlock(int blockId) throws DatabaseException {
        return complaintDAO.getComplaintsByBlock(blockId);
    }

    public List<Complaint> getComplaintsByStudent(int studentId) throws DatabaseException {
        return complaintDAO.getComplaintsByStudent(studentId);
    }

    public int getPendingComplaintsCount() throws DatabaseException {
        return complaintDAO.countPending();
    }
}
