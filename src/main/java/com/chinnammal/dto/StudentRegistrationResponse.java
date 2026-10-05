package com.chinnammal.dto;

import com.smarthostel.model.Student;

public class StudentRegistrationResponse {
    private Student student;
    private String username;
    private String temporaryPassword;

    public StudentRegistrationResponse() {}

    public StudentRegistrationResponse(Student student, String username, String temporaryPassword) {
        this.student = student;
        this.username = username;
        this.temporaryPassword = temporaryPassword;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
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
