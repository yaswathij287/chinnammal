package com.smarthostel.model;

import java.sql.Timestamp;

public class Student {
    private int studentId;
    private String name;
    private String department;
    private int year;
    private String phone;
    private String email;
    private Integer roomId; // null if unassigned
    private String bedNumber;
    private Timestamp createdAt;

    // Helper display fields
    private String roomNumber;
    private String blockName;
    private String username;
    private String status = "ACTIVE";
    private Integer userId;

    public Student() {}

    public Student(int studentId, String name, String department, int year, String phone,
                   String email, Integer roomId, String bedNumber, Timestamp createdAt) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.year = year;
        this.phone = phone;
        this.email = email;
        this.roomId = roomId;
        this.bedNumber = bedNumber;
        this.createdAt = createdAt;
    }

    public Student(String name, String department, int year, String phone, String email, Integer roomId, String bedNumber) {
        this.name = name;
        this.department = department;
        this.year = year;
        this.phone = phone;
        this.email = email;
        this.roomId = roomId;
        this.bedNumber = bedNumber;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getBlockName() { return blockName; }
    public void setBlockName(String blockName) { this.blockName = blockName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    @Override
    public String toString() {
        return name + " (" + department + " - Yr " + year + ")";
    }
}
