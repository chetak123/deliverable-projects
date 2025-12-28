package com.airtribe.learntrack.util;

import com.airtribe.learntrack.constants.AppConstants;

/**
 * Utility class for validating user inputs
 */
public class InputValidator {
    
    // Email regex pattern (simple validation)
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    /**
     * Validates if a string is not null or empty
     * @param input the string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }
    
    /**
     * Validates if a name meets length requirements
     * @param name the name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidName(String name) {
        if (!isNotEmpty(name)) {
            return false;
        }
        int length = name.trim().length();
        return length >= AppConstants.MIN_NAME_LENGTH && length <= AppConstants.MAX_NAME_LENGTH;
    }
    
    /**
     * Validates email format
     * @param email the email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }
    
    /**
     * Validates course duration
     * @param duration the duration in weeks
     * @return true if valid, false otherwise
     */
    public static boolean isValidDuration(int duration) {
        return duration >= AppConstants.MIN_COURSE_DURATION && 
               duration <= AppConstants.MAX_COURSE_DURATION;
    }
    
    /**
     * Validates if an ID is positive
     * @param id the ID to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidId(int id) {
        return id > 0;
    }
    
    // Private constructor to prevent instantiation
    private InputValidator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

