package com.smartparking.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all parking events.
 */
public abstract class ParkingEvent {
    private final String eventId;
    private final LocalDateTime timestamp;
    private final String eventType;

    protected ParkingEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return "ParkingEvent{" +
                "eventId='" + eventId + '\'' +
                ", timestamp=" + timestamp +
                ", eventType='" + eventType + '\'' +
                '}';
    }
}

