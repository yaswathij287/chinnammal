package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Fee;
import com.smarthostel.service.FeeService;

import java.util.List;

/**
 * Controller mediating fee payments and balance tracking.
 */
public class FeeController {

    private final FeeService feeService;

    public FeeController() {
        this.feeService = new FeeService();
    }

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    public List<Fee> getAllFees() throws DatabaseException {
        return feeService.getAllFees();
    }

    public List<Fee> getFeesByStudent(int studentId) throws DatabaseException {
        return feeService.getFeesByStudent(studentId);
    }

    public Fee addFee(Fee fee) throws ValidationException, DatabaseException {
        return feeService.addFee(fee);
    }

    public void recordPayment(int feeId, double amount) throws ValidationException, DatabaseException {
        feeService.recordPayment(feeId, amount);
    }

    public double getTotalPendingFees() throws DatabaseException {
        return feeService.getTotalPendingFees();
    }

    public double getTotalCollectedFees() throws DatabaseException {
        return feeService.getTotalCollectedFees();
    }
}
