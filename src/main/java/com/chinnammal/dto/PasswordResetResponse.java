package com.chinnammal.dto;

public class PasswordResetResponse {
    private String username;
    private String temporaryPassword;

    public PasswordResetResponse() {}

    public PasswordResetResponse(String username, String temporaryPassword) {
        this.username = username;
        this.temporaryPassword = temporaryPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }
}
