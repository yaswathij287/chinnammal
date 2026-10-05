package com.smarthostel.model;

/**
 * Result returned upon successful student registration and automatic user account creation.
 * Contains the created student entity, user entity, and the generated temporary password for initial display.
 */
public class StudentRegistrationResult {

    private final Student student;
    private final User user;
    private final String temporaryPassword;

    public StudentRegistrationResult(Student student, User user, String temporaryPassword) {
        this.student = student;
        this.user = user;
        this.temporaryPassword = temporaryPassword;
    }

    public Student getStudent() {
        return student;
    }

    public User getUser() {
        return user;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }
}
