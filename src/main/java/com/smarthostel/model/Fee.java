package com.smarthostel.model;

public class Fee {
    private int feeId;
    private int studentId;
    private double totalAmount;
    private double paidAmount;
    private double balance;
    private String dueDate;
    private String status; // 'PAID', 'PENDING', 'OVERDUE'

    // Helper display field
    private String studentName;

    public Fee() {}

    public Fee(int feeId, int studentId, double totalAmount, double paidAmount, double balance, String dueDate, String status) {
        this.feeId = feeId;
        this.studentId = studentId;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.balance = balance;
        this.dueDate = dueDate;
        this.status = status;
    }

    public Fee(int studentId, double totalAmount, double paidAmount, String dueDate) {
        this.studentId = studentId;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.balance = totalAmount - paidAmount;
        this.dueDate = dueDate;
        this.status = (balance <= 0) ? "PAID" : "PENDING";
    }

    public int getFeeId() { return feeId; }
    public void setFeeId(int feeId) { this.feeId = feeId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
