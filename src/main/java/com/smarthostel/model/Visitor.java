package com.smarthostel.model;

public class Visitor {
    private int visitorId;
    private int studentId;
    private String visitorName;
    private String relationship;
    private String phone;
    private String visitDate;
    private String visitTime;
    private String status; // 'PENDING', 'APPROVED', 'REJECTED', 'CHECKED_IN', 'CHECKED_OUT'

    // Helper display field
    private String studentName;

    public Visitor() {}

    public Visitor(int visitorId, int studentId, String visitorName, String relationship,
                   String phone, String visitDate, String visitTime, String status) {
        this.visitorId = visitorId;
        this.studentId = studentId;
        this.visitorName = visitorName;
        this.relationship = relationship;
        this.phone = phone;
        this.visitDate = visitDate;
        this.visitTime = visitTime;
        this.status = status;
    }

    public Visitor(int studentId, String visitorName, String relationship, String phone, String visitDate, String visitTime) {
        this.studentId = studentId;
        this.visitorName = visitorName;
        this.relationship = relationship;
        this.phone = phone;
        this.visitDate = visitDate;
        this.visitTime = visitTime;
        this.status = "PENDING";
    }

    public int getVisitorId() { return visitorId; }
    public void setVisitorId(int visitorId) { this.visitorId = visitorId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }

    public String getVisitTime() { return visitTime; }
    public void setVisitTime(String visitTime) { this.visitTime = visitTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
