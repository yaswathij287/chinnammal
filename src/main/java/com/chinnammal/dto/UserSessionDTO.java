package com.chinnammal.dto;

import com.smarthostel.model.Role;

public class UserSessionDTO {
    private int userId;
    private String username;
    private Role role;
    private String displayName;
    private Integer studentId;
    private Integer wardenId;
    private Integer assignedBlockId;
    private boolean forcePasswordChange;
    private String status = "ACTIVE";

    public UserSessionDTO() {}

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getWardenId() {
        return wardenId;
    }

    public void setWardenId(Integer wardenId) {
        this.wardenId = wardenId;
    }

    public Integer getAssignedBlockId() {
        return assignedBlockId;
    }

    public void setAssignedBlockId(Integer assignedBlockId) {
        this.assignedBlockId = assignedBlockId;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    // Alias for backwards compatibility
    public boolean isTempPassword() {
        return forcePasswordChange;
    }

    public void setTempPassword(boolean tempPassword) {
        this.forcePasswordChange = tempPassword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
