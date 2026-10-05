package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.LeaveRequest;
import com.smarthostel.service.LeaveService;

import java.util.List;

/**
 * Controller mediating student leave and out-pass requests.
 */
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController() {
        this.leaveService = new LeaveService();
    }

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    public List<LeaveRequest> getAllLeaveRequests() throws DatabaseException {
        return leaveService.getAllLeaveRequests();
    }

    public List<LeaveRequest> getLeaveRequestsByBlock(int blockId) throws DatabaseException {
        return leaveService.getLeaveRequestsByBlock(blockId);
    }

    public List<LeaveRequest> getLeaveRequestsByStudent(int studentId) throws DatabaseException {
        return leaveService.getLeaveRequestsByStudent(studentId);
    }

    public LeaveRequest applyLeave(LeaveRequest request) throws ValidationException, DatabaseException {
        return leaveService.applyLeave(request);
    }

    public void approveLeave(int leaveId, int wardenId) throws DatabaseException {
        leaveService.approveLeave(leaveId, wardenId);
    }

    public void rejectLeave(int leaveId, int wardenId) throws DatabaseException {
        leaveService.rejectLeave(leaveId, wardenId);
    }
}
