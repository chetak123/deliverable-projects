package com.smartparking.event;

import com.smartparking.enums.SpotSize;

/**
 * Event published when a parking spot availability changes.
 */
public class SpotAvailabilityChangedEvent extends ParkingEvent {
    private final String spotId;
    private final SpotSize spotSize;
    private final int floorNumber;
    private final boolean isNowAvailable;

    public SpotAvailabilityChangedEvent(String spotId, SpotSize spotSize, int floorNumber, boolean isNowAvailable) {
        super("SPOT_AVAILABILITY_CHANGED");
        this.spotId = spotId;
        this.spotSize = spotSize;
        this.floorNumber = floorNumber;
        this.isNowAvailable = isNowAvailable;
    }

    public String getSpotId() {
        return spotId;
    }

    public SpotSize getSpotSize() {
        return spotSize;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public boolean isNowAvailable() {
        return isNowAvailable;
    }

    @Override
    public String toString() {
        return "SpotAvailabilityChangedEvent{" +
                "spotId='" + spotId + '\'' +
                ", spotSize=" + spotSize +
                ", floor=" + floorNumber +
                ", available=" + isNowAvailable +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

