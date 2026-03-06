package com.smartparking.model;

import com.smartparking.enums.SpotSize;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a floor in the parking lot.
 */
public class ParkingFloor {
    private final int floorNumber;
    private final List<ParkingSpot> parkingSpots;

    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.parkingSpots = new ArrayList<>();
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public List<ParkingSpot> getParkingSpots() {
        return new ArrayList<>(parkingSpots);
    }

    public void addParkingSpot(ParkingSpot spot) {
        if (spot.getFloorNumber() != this.floorNumber) {
            throw new IllegalArgumentException("Spot floor number doesn't match this floor");
        }
        parkingSpots.add(spot);
    }

    /**
     * Get all available spots on this floor.
     */
    public List<ParkingSpot> getAvailableSpots() {
        return parkingSpots.stream()
                .filter(spot -> !spot.isOccupied())
                .collect(Collectors.toList());
    }

    /**
     * Get available spots of a specific size.
     */
    public List<ParkingSpot> getAvailableSpotsBySize(SpotSize size) {
        return parkingSpots.stream()
                .filter(spot -> !spot.isOccupied() && spot.getSpotSize() == size)
                .collect(Collectors.toList());
    }

    /**
     * Get total number of spots on this floor.
     */
    public int getTotalSpots() {
        return parkingSpots.size();
    }

    /**
     * Get number of available spots on this floor.
     */
    public int getAvailableSpotCount() {
        return (int) parkingSpots.stream()
                .filter(spot -> !spot.isOccupied())
                .count();
    }

    @Override
    public String toString() {
        return "ParkingFloor{" +
                "floorNumber=" + floorNumber +
                ", totalSpots=" + getTotalSpots() +
                ", availableSpots=" + getAvailableSpotCount() +
                '}';
    }
}

