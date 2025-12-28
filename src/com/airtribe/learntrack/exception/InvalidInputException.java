package com.airtribe.learntrack.exception;

/**
 * Custom exception thrown when user input is invalid
 * Demonstrates custom exception handling
 */
public class InvalidInputException extends Exception {
    
    /**
     * Constructor with message
     * @param message the error message
     */
    public InvalidInputException(String message) {
        super(message);
    }
    
    /**
     * Constructor with message and cause
     * @param message the error message
     * @param cause the cause of the exception
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}

