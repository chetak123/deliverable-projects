package com.smartparking.model;

import com.smartparking.enums.SpotSize;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a parking spot in the parking lot.
 * Thread-safe implementation with optimistic locking support.
 */
public class ParkingSpot {
    private final String spotId;
    private final int floorNumber;
    private final SpotSize spotSize;
    private final int distanceFromEntrance; // in meters
    private boolean isOccupied;
    private String currentVehicleLicensePlate;
    private long version; // For optimistic locking
    private final ReentrantLock lock; // For pessimistic locking

    public ParkingSpot(String spotId, int floorNumber, SpotSize spotSize, int distanceFromEntrance) {
        if (spotId == null || spotId.trim().isEmpty()) {
            throw new IllegalArgumentException("Spot ID cannot be null or empty");
        }
        if (spotSize == null) {
            throw new IllegalArgumentException("Spot size cannot be null");
        }
        this.spotId = spotId;
        this.floorNumber = floorNumber;
        this.spotSize = spotSize;
        this.distanceFromEntrance = distanceFromEntrance;
        this.isOccupied = false;
        this.currentVehicleLicensePlate = null;
        this.version = 0L;
        this.lock = new ReentrantLock();
    }

    public String getSpotId() {
        return spotId;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public SpotSize getSpotSize() {
        return spotSize;
    }

    public int getDistanceFromEntrance() {
        return distanceFromEntrance;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public String getCurrentVehicleLicensePlate() {
        return currentVehicleLicensePlate;
    }

    public long getVersion() {
        return version;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    /**
     * Occupy the spot with a vehicle (thread-safe).
     */
    public synchronized boolean occupy(String vehicleLicensePlate) {
        if (isOccupied) {
            return false;
        }
        this.isOccupied = true;
        this.currentVehicleLicensePlate = vehicleLicensePlate;
        this.version++;
        return true;
    }

    /**
     * Release the spot (thread-safe).
     */
    public synchronized boolean release() {
        if (!isOccupied) {
            return false;
        }
        this.isOccupied = false;
        this.currentVehicleLicensePlate = null;
        this.version++;
        return true;
    }

    /**
     * Occupy with version check (optimistic locking).
     */
    public synchronized boolean occupyWithVersionCheck(String vehicleLicensePlate, long expectedVersion) {
        if (this.version != expectedVersion || isOccupied) {
            return false;
        }
        return occupy(vehicleLicensePlate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParkingSpot that = (ParkingSpot) o;
        return spotId.equals(that.spotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spotId);
    }

    @Override
    public String toString() {
        return "ParkingSpot{" +
                "spotId='" + spotId + '\'' +
                ", floor=" + floorNumber +
                ", size=" + spotSize +
                ", occupied=" + isOccupied +
                ", vehicle='" + currentVehicleLicensePlate + '\'' +
                '}';
    }
}

