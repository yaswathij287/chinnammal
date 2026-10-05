package com.smarthostel.model;

/**
 * Result object returned when a warden is registered with an auto-generated login account.
 */
public class WardenRegistrationResult {
    private final Warden warden;
    private final User user;
    private final String temporaryPassword;

    public WardenRegistrationResult(Warden warden, User user, String temporaryPassword) {
        this.warden = warden;
        this.user = user;
        this.temporaryPassword = temporaryPassword;
    }

    public Warden getWarden() {
        return warden;
    }

    public User getUser() {
        return user;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }
}
