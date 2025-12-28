package com.airtribe.learntrack.exception;

/**
 * Custom exception thrown when an entity (Student, Course, Enrollment) is not found
 * Demonstrates custom exception handling
 */
public class EntityNotFoundException extends Exception {
    
    /**
     * Constructor with message
     * @param message the error message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     * @param message the error message
     * @param cause the cause of the exception
     */
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

