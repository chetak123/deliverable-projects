package com.library.pattern.observer;

/**
 * Subject interface for the Observer pattern.
 * Subjects maintain a list of observers and notify them of events.
 */
public interface Subject {
    /**
     * Attach an observer to this subject.
     * 
     * @param observer The observer to attach
     */
    void attach(Observer observer);

    /**
     * Detach an observer from this subject.
     * 
     * @param observer The observer to detach
     */
    void detach(Observer observer);

    /**
     * Notify all observers of an event.
     * 
     * @param event The event to notify observers about
     */
    void notifyObservers(String event);
}

