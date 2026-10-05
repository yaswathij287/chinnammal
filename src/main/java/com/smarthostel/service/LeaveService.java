package com.smarthostel.service;

import com.smarthostel.dao.LeaveRequestDAO;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.LeaveRequest;
import com.smarthostel.util.Validator;

import java.util.List;

/**
 * Service managing student hostel out-pass and leave authorization.
 */
public class LeaveService {

    private final LeaveRequestDAO leaveRequestDAO;

    public LeaveService() {
        this.leaveRequestDAO = new LeaveRequestDAO();
    }

    public LeaveService(LeaveRequestDAO leaveRequestDAO) {
        this.leaveRequestDAO = leaveRequestDAO;
    }

    public LeaveRequest applyLeave(LeaveRequest request) throws ValidationException, DatabaseException {
        Validator.validateDateRange(request.getFromDate(), request.getToDate());
        Validator.requireNonEmpty(request.getReason(), "Reason for Leave");
        request.setStatus("PENDING");
        return leaveRequestDAO.addLeaveRequest(request);
    }

    public void approveLeave(int leaveId, int approvedBy) throws DatabaseException {
        leaveRequestDAO.updateStatus(leaveId, "APPROVED", approvedBy);
    }

    public void rejectLeave(int leaveId, int approvedBy) throws DatabaseException {
        leaveRequestDAO.updateStatus(leaveId, "REJECTED", approvedBy);
    }

    public List<LeaveRequest> getAllLeaveRequests() throws DatabaseException {
        return leaveRequestDAO.getAllLeaveRequests();
    }

    public List<LeaveRequest> getLeaveRequestsByBlock(int blockId) throws DatabaseException {
        return leaveRequestDAO.getLeaveRequestsByBlock(blockId);
    }

    public List<LeaveRequest> getLeaveRequestsByStudent(int studentId) throws DatabaseException {
        return leaveRequestDAO.getLeaveRequestsByStudent(studentId);
    }

    public int getPendingLeaveCount() throws DatabaseException {
        return leaveRequestDAO.countPending();
    }
}
