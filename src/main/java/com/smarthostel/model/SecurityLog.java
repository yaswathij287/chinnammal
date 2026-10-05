package com.smarthostel.model;

import java.sql.Timestamp;

public class SecurityLog {
    private int logId;
    private Integer studentId;
    private String eventType; // 'ENTRY', 'EXIT'
    private Timestamp eventTime;
    private String gate;
    private String verificationMethod; // 'CARD', 'MANUAL', 'FACE'

    // Helper display fields
    private String studentName;
    private String status; // 'NORMAL', 'LATE', 'UNKNOWN'

    public SecurityLog() {}

    public SecurityLog(int logId, Integer studentId, String eventType, Timestamp eventTime,
                       String gate, String verificationMethod) {
        this.logId = logId;
        this.studentId = studentId;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.gate = gate;
        this.verificationMethod = verificationMethod;
    }

    public SecurityLog(Integer studentId, String eventType, String gate, String verificationMethod) {
        this.studentId = studentId;
        this.eventType = eventType;
        this.gate = gate;
        this.verificationMethod = verificationMethod;
        this.eventTime = new Timestamp(System.currentTimeMillis());
    }

    public SecurityLog(Integer studentId, String eventType, Timestamp eventTime, String gate, String verificationMethod) {
        this.studentId = studentId;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.gate = gate;
        this.verificationMethod = verificationMethod;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Timestamp getEventTime() { return eventTime; }
    public void setEventTime(Timestamp eventTime) { this.eventTime = eventTime; }

    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }

    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
