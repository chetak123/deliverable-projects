package com.library.pattern.observer;

/**
 * Observer interface for the Observer pattern.
 * Observers are notified when events occur.
 */
public interface Observer {
    /**
     * Called when an event occurs.
     * 
     * @param event The event that occurred
     */
    void update(String event);
}

