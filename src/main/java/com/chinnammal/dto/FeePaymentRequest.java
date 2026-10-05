package com.chinnammal.dto;

public class FeePaymentRequest {
    private int feeId;
    private double amount;

    public FeePaymentRequest() {}

    public int getFeeId() {
        return feeId;
    }

    public void setFeeId(int feeId) {
        this.feeId = feeId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
