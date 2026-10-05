package com.smarthostel.model;

public enum Role {
    ADMIN,
    WARDEN,
    STUDENT;

    public static Role fromString(String roleStr) {
        if (roleStr == null) return null;
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
