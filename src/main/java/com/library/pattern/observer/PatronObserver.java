package com.library.pattern.observer;

import com.library.model.Patron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Observer that represents a patron receiving notifications.
 * Implements the Observer pattern.
 */
public class PatronObserver implements Observer {
    private static final Logger logger = LoggerFactory.getLogger(PatronObserver.class);
    
    private final Patron patron;

    public PatronObserver(Patron patron) {
        this.patron = patron;
    }

    @Override
    public void update(String event) {
        logger.info("Notification sent to patron {}: {}", patron.getName(), event);
        // In a real system, this would send an email or SMS
        System.out.println(String.format("📧 Notification to %s (%s): %s", 
                patron.getName(), patron.getEmail(), event));
    }

    public Patron getPatron() {
        return patron;
    }
}

