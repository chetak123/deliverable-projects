package com.smartparking.event;

/**
 * Interface for event listeners.
 */
@FunctionalInterface
public interface EventListener<T extends ParkingEvent> {
    /**
     * Handle the event.
     */
    void onEvent(T event);
}

