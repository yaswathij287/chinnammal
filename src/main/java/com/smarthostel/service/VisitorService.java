package com.smarthostel.service;

import com.smarthostel.dao.VisitorDAO;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Visitor;
import com.smarthostel.util.Validator;

import java.util.List;

/**
 * Service managing visitor entry requests, approvals, and logs.
 */
public class VisitorService {

    private final VisitorDAO visitorDAO;

    public VisitorService() {
        this.visitorDAO = new VisitorDAO();
    }

    public VisitorService(VisitorDAO visitorDAO) {
        this.visitorDAO = visitorDAO;
    }

    public Visitor requestVisitor(Visitor visitor) throws ValidationException, DatabaseException {
        Validator.requireNonEmpty(visitor.getVisitorName(), "Visitor Name");
        Validator.requireNonEmpty(visitor.getRelationship(), "Relationship");
        Validator.validatePhone(visitor.getPhone());
        Validator.requireNonEmpty(visitor.getVisitDate(), "Visit Date");
        visitor.setStatus("PENDING");
        return visitorDAO.addVisitor(visitor);
    }

    public void approveVisitor(int visitorId) throws DatabaseException {
        visitorDAO.updateStatus(visitorId, "APPROVED");
    }

    public void rejectVisitor(int visitorId) throws DatabaseException {
        visitorDAO.updateStatus(visitorId, "REJECTED");
    }

    public List<Visitor> getAllVisitors() throws DatabaseException {
        return visitorDAO.getAllVisitors();
    }

    public List<Visitor> getVisitorsByBlock(int blockId) throws DatabaseException {
        return visitorDAO.getVisitorsByBlock(blockId);
    }

    public List<Visitor> getVisitorsByStudent(int studentId) throws DatabaseException {
        return visitorDAO.getVisitorsByStudent(studentId);
    }

    public int getPendingVisitorsCount() throws DatabaseException {
        return visitorDAO.countPending();
    }
}
