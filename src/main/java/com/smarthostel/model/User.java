package com.smarthostel.model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private Role role;
    private Integer linkedStudentId;
    private Integer linkedWardenId;
    private boolean forcePasswordChange;
    private String status = "ACTIVE";
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {}

    public User(int id, String username, String passwordHash, Role role, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public User(int id, String username, String passwordHash, Role role, Integer linkedStudentId, Integer linkedWardenId,
                boolean forcePasswordChange, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.linkedStudentId = linkedStudentId;
        this.linkedWardenId = linkedWardenId;
        this.forcePasswordChange = forcePasswordChange;
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.passwordHash = password;
        this.role = role;
    }

    public User(String username, String password, Role role, Integer studentId, boolean isTempPassword) {
        this.username = username;
        this.passwordHash = password;
        this.role = role;
        this.linkedStudentId = studentId;
        this.forcePasswordChange = isTempPassword;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // Backwards-compatible aliases for password
    public String getPassword() { return passwordHash; }
    public void setPassword(String password) { this.passwordHash = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Integer getLinkedStudentId() { return linkedStudentId; }
    public void setLinkedStudentId(Integer linkedStudentId) { this.linkedStudentId = linkedStudentId; }

    // Backwards-compatible aliases for studentId
    public Integer getStudentId() { return linkedStudentId; }
    public void setStudentId(Integer studentId) { this.linkedStudentId = studentId; }

    public Integer getLinkedWardenId() { return linkedWardenId; }
    public void setLinkedWardenId(Integer linkedWardenId) { this.linkedWardenId = linkedWardenId; }

    // Alias for wardenId
    public Integer getWardenId() { return linkedWardenId; }
    public void setWardenId(Integer wardenId) { this.linkedWardenId = wardenId; }

    public boolean isForcePasswordChange() { return forcePasswordChange; }
    public void setForcePasswordChange(boolean forcePasswordChange) { this.forcePasswordChange = forcePasswordChange; }

    // Backwards-compatible alias for isTempPassword
    public boolean isTempPassword() { return forcePasswordChange; }
    public void setTempPassword(boolean tempPassword) { this.forcePasswordChange = tempPassword; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role +
                ", linkedStudentId=" + linkedStudentId + ", linkedWardenId=" + linkedWardenId +
                ", forcePasswordChange=" + forcePasswordChange + ", status='" + status + "'}";
    }
}
