package com.shadsluiter.eventsapp.security;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing user input to help prevent SQL Injection, 
 * control character injection, and general malicious payloads.
 * 
 * This class demonstrates multiple sanitization approaches based on 
 * the input context (username, password, numeric, generic text).
 */
public class InputSanitizer {

    /** Username allowed characters: letters, numbers, underscore, hyphen, dot */
    private static final Pattern USERNAME_ALLOWED = Pattern.compile("[^a-zA-Z0-9_.-]");

    /** Removes all non-printable control characters except CR, LF, TAB for passwords */
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");

    /** Allows only numeric digits */
    private static final Pattern NUMERIC_ONLY = Pattern.compile("[^0-9]");

    /** SQL keywords and operators to filter for additional defense-in-depth */
    private static final String[] DANGEROUS_KEYWORDS = {
        "--", ";", "/\\*", "\\*/", "xp_", "exec", "union", "select",
        "insert", "update", "delete", "drop", "create", "alter"
    };

    /** Private constructor: prevents instantiation (utility class pattern) */
    private InputSanitizer() {}

    /**
     * Sanitizes username input.
     * 
     * Only allows letters, digits, underscores, hyphens, and dots.
     *
     * @param username input username string
     * @return sanitized username string
     */
    public static String sanitizeUsername(String username) {
        if (username == null) {
            return null;
        }
        username = username.trim();
        return USERNAME_ALLOWED.matcher(username).replaceAll("");
    }

    /**
     * Sanitizes numeric input.
     * 
     * Removes all non-digit characters.
     *
     * @param numeric input string expected to contain digits
     * @return sanitized numeric string
     */
    public static String sanitizeNumeric(String numeric) {
        if (numeric == null) {
            return null;
        }
        numeric = numeric.trim();
        return NUMERIC_ONLY.matcher(numeric).replaceAll("");
    }

    /**
     * Sanitizes password input.
     * 
     * Allows full printable characters, removes non-printable control characters 
     * (except for carriage return, newline, and tab).
     *
     * @param password input password string
     * @return sanitized password string
     */
    public static String sanitizePassword(String password) {
        if (password == null) {
            return null;
        }
        password = password.trim();
        return CONTROL_CHARS.matcher(password).replaceAll("");
    }

    /**
     * General-purpose input sanitizer (for text fields like descriptions, comments, etc).
     * 
     * - Enforces 100-character maximum length.
     * - Escapes single quotes.
     * - Removes SQL keywords (defense-in-depth, not a substitute for parameterized queries).
     * - Allows only alphanumeric characters and spaces after filtering.
     *
     * @param text input string
     * @return sanitized string
     * @throws IllegalArgumentException if input exceeds allowed length
     */
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
            text = text.replaceAll("(?i)" + keyword, ""); // Remove keywords case-insensitively
        }

        return text.replaceAll("[^a-zA-Z0-9 ]", ""); // Allow only alphanumeric and spaces
    }
}
