package com.smarthostel.util;

import com.smarthostel.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validation utility for user inputs across forms and services.
 */
public class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10}$");

    private Validator() {}

    public static void requireNonEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) return; // Optional if allowed
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void validatePhone(String phone) throws ValidationException {
        if (phone == null || phone.trim().isEmpty()) return; // Optional if allowed
        String sanitized = phone.replaceAll("[\\s-]", "");
        if (!PHONE_PATTERN.matcher(sanitized).matches()) {
            throw new ValidationException("Phone number must be a valid 10-digit number.");
        }
    }

    public static void validatePositiveNumber(double value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    public static void validateNonNegativeNumber(double value, String fieldName) throws ValidationException {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }

    public static void validatePositiveInt(int value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be at least 1.");
        }
    }

    public static void validateDateRange(String fromDateStr, String toDateStr) throws ValidationException {
        requireNonEmpty(fromDateStr, "From Date");
        requireNonEmpty(toDateStr, "To Date");
        try {
            LocalDate from = LocalDate.parse(fromDateStr);
            LocalDate to = LocalDate.parse(toDateStr);
            if (to.isBefore(from)) {
                throw new ValidationException("To Date cannot be before From Date.");
            }
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Expected YYYY-MM-DD.");
        }
    }

    public static void validateYear(int year) throws ValidationException {
        if (year < 1 || year > 4) {
            throw new ValidationException("Academic Year must be between 1 and 4.");
        }
    }

    public static void validatePasswordRequirements(String password) throws ValidationException {
        requireNonEmpty(password, "Password");
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("Password must contain at least one uppercase letter (A-Z).");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("Password must contain at least one lowercase letter (a-z).");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new ValidationException("Password must contain at least one number (0-9).");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new ValidationException("Password must contain at least one special character (e.g. @, #, $, %, &, *).");
        }
    }
}
