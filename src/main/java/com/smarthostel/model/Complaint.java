package com.smarthostel.model;

import java.sql.Timestamp;

public class Complaint {
    private int complaintId;
    private int studentId;
    private String category;
    private String description;
    private Integer roomId;
    private String status;   // 'OPEN', 'IN_PROGRESS', 'RESOLVED'
    private String priority; // 'LOW', 'MEDIUM', 'HIGH'
    private Timestamp createdAt;
    private Timestamp resolvedAt;

    // Helper display fields
    private String studentName;
    private String roomNumber;

    public Complaint() {}

    public Complaint(int complaintId, int studentId, String category, String description,
                     Integer roomId, String status, String priority, Timestamp createdAt, Timestamp resolvedAt) {
        this.complaintId = complaintId;
        this.studentId = studentId;
        this.category = category;
        this.description = description;
        this.roomId = roomId;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public Complaint(int studentId, String category, String description, Integer roomId, String priority) {
        this.studentId = studentId;
        this.category = category;
        this.description = description;
        this.roomId = roomId;
        this.priority = priority;
        this.status = "OPEN";
    }

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}
