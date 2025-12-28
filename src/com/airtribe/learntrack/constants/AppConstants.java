package com.airtribe.learntrack.constants;

/**
 * Application-wide constants
 */
public class AppConstants {
    
    // Application Info
    public static final String APP_NAME = "LearnTrack";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_DESCRIPTION = "Student & Course Management System";
    
    // Validation Constants
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MIN_COURSE_DURATION = 1;
    public static final int MAX_COURSE_DURATION = 52; // weeks
    
    // Messages
    public static final String INVALID_INPUT_MSG = "Invalid input. Please try again.";
    public static final String OPERATION_SUCCESS_MSG = "Operation completed successfully!";
    public static final String OPERATION_FAILED_MSG = "Operation failed. Please try again.";
    public static final String ENTITY_NOT_FOUND_MSG = "Entity not found.";
    
    // Private constructor to prevent instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

