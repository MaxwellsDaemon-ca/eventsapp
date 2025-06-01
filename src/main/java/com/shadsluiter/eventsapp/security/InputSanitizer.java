package com.shadsluiter.eventsapp.security;

import java.util.regex.Pattern;

public class InputSanitizer {

    // Username allowed: alphanumeric + underscore, hyphen, dot
    private static final Pattern USERNAME_ALLOWED = Pattern.compile("[^a-zA-Z0-9_.-]");
    
    // Remove control characters for passwords
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");

    private static final Pattern NUMERIC_ONLY = Pattern.compile("[^0-9]");

    private static final String[] DANGEROUS_KEYWORDS = {
        "--", ";", "/\\*", "\\*/", "xp_", "exec", "union", "select", "insert", "update", "delete", "drop", "create", "alter"
    };

    private InputSanitizer() {
        // private constructor: utility class
    }

    /**
     * Sanitizes username input:
     * Allows: letters, numbers, underscore, hyphen, dot
     */
    public static String sanitizeUsername(String username) {
        if (username == null) {
            return null;
        }
        username = username.trim();
        return USERNAME_ALLOWED.matcher(username).replaceAll("");
    }

    /**
     * Sanitizes numeric input:
     * Allows only digits, removes all other characters.
     */
    public static String sanitizeNumeric(String numeric) {
        if (numeric == null) {
            return null;
        }
        numeric = numeric.trim();
        return NUMERIC_ONLY.matcher(numeric).replaceAll("");
    }

    /**
     * Sanitizes password input:
     * Allows full printable characters but removes control characters.
     */
    public static String sanitizePassword(String password) {
        if (password == null) {
            return null;
        }
        password = password.trim();
        return CONTROL_CHARS.matcher(password).replaceAll("");
    }

    
    public static String sanitizeInput(String text) {
        if (text == null) {
            return null;
        }
        text = text.trim();

        if (text.length() > 100) {
            throw new IllegalArgumentException("Input exceeds maximum length of 100 characters");
        }

        text = text.replaceAll("'", "''"); // Escape single quotes
        for (String keyword : DANGEROUS_KEYWORDS) {
            text = text.replaceAll("(?i)" + keyword, ""); // Remove dangerous keywords case-insensitively
        }

        return text.replaceAll("[^a-zA-Z0-9 ]", ""); // Allow only alphanumeric characters and spaces
        
    }
}
