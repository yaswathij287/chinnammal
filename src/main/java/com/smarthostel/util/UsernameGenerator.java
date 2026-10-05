package com.smarthostel.util;

import com.smarthostel.dao.UserDAO;
import com.smarthostel.exception.DatabaseException;

/**
 * Utility to generate unique usernames based on a student's full name.
 * Format:
 * "Arun Kumar" -> "arun.kumar"
 * If "arun.kumar" exists -> "arun.kumar1", "arun.kumar2", etc.
 */
public class UsernameGenerator {

    private UsernameGenerator() {}

    public static String generateUsername(String fullName, UserDAO userDAO) throws DatabaseException {
        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "student";
        }

        // 1. Lowercase, remove special characters except spaces, replace spaces with dot
        String sanitized = fullName.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", ".");

        if (sanitized.isEmpty()) {
            sanitized = "student";
        }

        // 2. Check if base username is already taken
        if (!userDAO.existsByUsername(sanitized)) {
            return sanitized;
        }

        // 3. Incrementally append numbers until a unique username is found
        int counter = 1;
        while (true) {
            String candidate = sanitized + counter;
            if (!userDAO.existsByUsername(candidate)) {
                return candidate;
            }
            counter++;
        }
    }
}
