package com.library.pattern.factory;

import com.library.model.Patron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating Patron objects.
 * Implements the Factory pattern to encapsulate object creation.
 */
public class PatronFactory {
    private static final Logger logger = LoggerFactory.getLogger(PatronFactory.class);

    /**
     * Creates a new Patron with validation.
     * 
     * @param name The patron's name
     * @param email The patron's email
     * @param phoneNumber The patron's phone number
     * @return A new Patron instance
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static Patron createPatron(String name, String email, String phoneNumber) {
        try {
            Patron patron = new Patron(name, email, phoneNumber);
            logger.info("Created new patron: {}", patron);
            return patron;
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create patron: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Creates a Patron from a CSV-like string format.
     * Format: "Name,Email,PhoneNumber"
     * 
     * @param patronData The patron data string
     * @return A new Patron instance
     */
    public static Patron createPatronFromString(String patronData) {
        String[] parts = patronData.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid patron data format. Expected: Name,Email,PhoneNumber");
        }
        
        String name = parts[0].trim();
        String email = parts[1].trim();
        String phoneNumber = parts[2].trim();
        
        return createPatron(name, email, phoneNumber);
    }
}

