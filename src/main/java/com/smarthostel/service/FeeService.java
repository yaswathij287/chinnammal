package com.smarthostel.service;

import com.smarthostel.dao.FeeDAO;
import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.Fee;
import com.smarthostel.util.Validator;

import java.util.List;

/**
 * Service managing fee structures, balance calculations, and payment tracking.
 */
public class FeeService {

    private final FeeDAO feeDAO;

    public FeeService() {
        this.feeDAO = new FeeDAO();
    }

    public FeeService(FeeDAO feeDAO) {
        this.feeDAO = feeDAO;
    }

    public Fee addFee(Fee fee) throws ValidationException, DatabaseException {
        Validator.validatePositiveNumber(fee.getTotalAmount(), "Total Fee Amount");
        Validator.validateNonNegativeNumber(fee.getPaidAmount(), "Paid Amount");

        if (fee.getPaidAmount() > fee.getTotalAmount()) {
            throw new ValidationException("Paid amount cannot exceed total fee amount.");
        }

        fee.setBalance(fee.getTotalAmount() - fee.getPaidAmount());
        fee.setStatus(fee.getBalance() <= 0 ? "PAID" : "PENDING");
        return feeDAO.addFee(fee);
    }

    public void recordPayment(int feeId, double amount) throws ValidationException, DatabaseException {
        Validator.validatePositiveNumber(amount, "Payment Amount");

        Fee fee = feeDAO.getFeeById(feeId);
        if (fee == null) {
            throw new ValidationException("Fee record not found.");
        }

        if (amount > fee.getBalance()) {
            throw new ValidationException("Payment amount (" + amount + ") exceeds current balance (" + fee.getBalance() + ").");
        }

        feeDAO.recordPayment(feeId, amount);
    }

    public List<Fee> getAllFees() throws DatabaseException {
        return feeDAO.getAllFees();
    }

    public List<Fee> getFeesByStudent(int studentId) throws DatabaseException {
        return feeDAO.getFeesByStudent(studentId);
    }

    public double getTotalPendingFees() throws DatabaseException {
        return feeDAO.getTotalPendingFees();
    }

    public double getTotalCollectedFees() throws DatabaseException {
        return feeDAO.getTotalCollectedFees();
    }
}
