package com.smarthostel.util;

import com.smarthostel.model.Role;
import com.smarthostel.model.Student;
import com.smarthostel.model.User;
import com.smarthostel.model.Warden;

/**
 * Manages the authenticated user's session state.
 */
public class SessionManager {

    private static User currentUser = null;
    private static Warden currentWarden = null;
    private static Student currentStudent = null;

    private SessionManager() {}

    public static void startSession(User user, Warden warden, Student student) {
        currentUser = user;
        currentWarden = warden;
        currentStudent = student;
    }

    public static void clearSession() {
        currentUser = null;
        currentWarden = null;
        currentStudent = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Role getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static Warden getCurrentWarden() {
        return currentWarden;
    }

    public static void setCurrentWarden(Warden warden) {
        currentWarden = warden;
    }

    public static Student getCurrentStudent() {
        return currentStudent;
    }

    public static void setCurrentStudent(Student student) {
        currentStudent = student;
    }

    public static String getDisplayName() {
        if (currentUser == null) return "Guest";
        if (currentStudent != null) return currentStudent.getName();
        if (currentWarden != null) return currentWarden.getName();
        return currentUser.getUsername();
    }
}
