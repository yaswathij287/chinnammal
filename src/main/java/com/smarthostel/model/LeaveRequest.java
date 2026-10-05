package com.smarthostel.model;

public class LeaveRequest {
    private int leaveId;
    private int studentId;
    private String fromDate;
    private String toDate;
    private String reason;
    private String status; // 'PENDING', 'APPROVED', 'REJECTED'
    private Integer approvedBy;

    // Helper display fields
    private String studentName;
    private String approvedByName;

    public LeaveRequest() {}

    public LeaveRequest(int leaveId, int studentId, String fromDate, String toDate,
                        String reason, String status, Integer approvedBy) {
        this.leaveId = leaveId;
        this.studentId = studentId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.reason = reason;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    public LeaveRequest(int studentId, String fromDate, String toDate, String reason) {
        this.studentId = studentId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.reason = reason;
        this.status = "PENDING";
    }

    public int getLeaveId() { return leaveId; }
    public void setLeaveId(int leaveId) { this.leaveId = leaveId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getFromDate() { return fromDate; }
    public void setFromDate(String fromDate) { this.fromDate = fromDate; }

    public String getToDate() { return toDate; }
    public void setToDate(String toDate) { this.toDate = toDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }
}
