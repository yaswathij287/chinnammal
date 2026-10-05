package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Visitor;
import com.smarthostel.service.VisitorService;

import java.util.List;

/**
 * Controller mediating visitor request submissions and approvals.
 */
public class VisitorController {

    private final VisitorService visitorService;

    public VisitorController() {
        this.visitorService = new VisitorService();
    }

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    public List<Visitor> getAllVisitors() throws DatabaseException {
        return visitorService.getAllVisitors();
    }

    public List<Visitor> getVisitorsByBlock(int blockId) throws DatabaseException {
        return visitorService.getVisitorsByBlock(blockId);
    }

    public List<Visitor> getVisitorsByStudent(int studentId) throws DatabaseException {
        return visitorService.getVisitorsByStudent(studentId);
    }

    public Visitor requestVisitor(Visitor visitor) throws ValidationException, DatabaseException {
        return visitorService.requestVisitor(visitor);
    }

    public void approveVisitor(int visitorId) throws DatabaseException {
        visitorService.approveVisitor(visitorId);
    }

    public void rejectVisitor(int visitorId) throws DatabaseException {
        visitorService.rejectVisitor(visitorId);
    }
}
