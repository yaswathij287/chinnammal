package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.exception.InvalidLoginException;
import com.smarthostel.exception.ValidationException;
import com.smarthostel.model.User;
import com.smarthostel.service.AuthService;

/**
 * Controller mediating login requests and authentication events.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public User handleLogin(String username, String password) throws ValidationException, InvalidLoginException, DatabaseException {
        return authService.login(username, password);
    }

    public void handleLogout() {
        authService.logout();
    }

    public void changePassword(int userId, String newPassword) throws ValidationException, DatabaseException {
        authService.changePassword(userId, newPassword);
    }
}
