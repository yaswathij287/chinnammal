package com.smarthostel.model;

import java.sql.Timestamp;

public class Warden {
    private int wardenId;
    private String name;
    private String employeeId;
    private String department;
    private String phone;
    private String email;
    private Integer assignedBlock; // null if unassigned
    private String address;
    private Timestamp createdAt;

    // Helper display fields (from block / linked user account)
    private String blockName;
    private String username;
    private String status = "ACTIVE";
    private Integer userId;

    public Warden() {}

    public Warden(int wardenId, String name, String phone, String email, Integer assignedBlock, Timestamp createdAt) {
        this.wardenId = wardenId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.assignedBlock = assignedBlock;
        this.createdAt = createdAt;
    }

    public Warden(String name, String phone, String email, Integer assignedBlock) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.assignedBlock = assignedBlock;
    }

    public Warden(String name, String employeeId, String department, String phone, String email, Integer assignedBlock, String address) {
        this.name = name;
        this.employeeId = employeeId;
        this.department = department;
        this.phone = phone;
        this.email = email;
        this.assignedBlock = assignedBlock;
        this.address = address;
    }

    public int getWardenId() { return wardenId; }
    public void setWardenId(int wardenId) { this.wardenId = wardenId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAssignedBlock() { return assignedBlock; }
    public void setAssignedBlock(Integer assignedBlock) { this.assignedBlock = assignedBlock; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

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
        return name + (employeeId != null ? " (" + employeeId + ")" : "") + (blockName != null ? " - " + blockName : "");
    }
}
