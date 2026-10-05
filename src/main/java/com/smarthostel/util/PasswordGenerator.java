package com.smarthostel.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility to generate secure random temporary passwords for new student registrations.
 * Generates at least 8 characters including uppercase, lowercase, digits, and special characters.
 */
public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ"; // omitted ambiguous chars
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "@#$%&*!";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS + SPECIAL;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_LENGTH = 8;

    private PasswordGenerator() {}

    public static String generateTemporaryPassword() {
        return generateTemporaryPassword(DEFAULT_LENGTH);
    }

    public static String generateTemporaryPassword(int length) {
        if (length < 8) {
            length = 8;
        }

        List<Character> passwordChars = new ArrayList<>();

        // Ensure at least one from each required character category
        passwordChars.add(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        passwordChars.add(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        passwordChars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        passwordChars.add(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        // Fill remaining characters from complete character set
        for (int i = 4; i < length; i++) {
            passwordChars.add(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        // Shuffle character order so positions are non-deterministic
        Collections.shuffle(passwordChars, RANDOM);

        StringBuilder sb = new StringBuilder(passwordChars.size());
        for (char c : passwordChars) {
            sb.append(c);
        }
        return sb.toString();
    }
}
