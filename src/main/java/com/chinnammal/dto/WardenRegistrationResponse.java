package com.chinnammal.dto;

import com.smarthostel.model.Warden;

public class WardenRegistrationResponse {
    private Warden warden;
    private String username;
    private String temporaryPassword;

    public WardenRegistrationResponse() {}

    public WardenRegistrationResponse(Warden warden, String username, String temporaryPassword) {
        this.warden = warden;
        this.username = username;
        this.temporaryPassword = temporaryPassword;
    }

    public Warden getWarden() {
        return warden;
    }

    public void setWarden(Warden warden) {
        this.warden = warden;
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
