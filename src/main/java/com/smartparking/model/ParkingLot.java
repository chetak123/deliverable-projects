package com.smartparking.model;

import com.smartparking.enums.SpotSize;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the entire parking lot with multiple floors.
 */
public class ParkingLot {
    private final String parkingLotId;
    private final String name;
    private final String address;
    private final List<ParkingFloor> floors;

    public ParkingLot(String parkingLotId, String name, String address) {
        this.parkingLotId = parkingLotId;
        this.name = name;
        this.address = address;
        this.floors = new ArrayList<>();
    }

    public String getParkingLotId() {
        return parkingLotId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public List<ParkingFloor> getFloors() {
        return new ArrayList<>(floors);
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    /**
     * Get all parking spots across all floors.
     */
    public List<ParkingSpot> getAllParkingSpots() {
        return floors.stream()
                .flatMap(floor -> floor.getParkingSpots().stream())
                .collect(Collectors.toList());
    }

    /**
     * Get all available spots across all floors.
     */
    public List<ParkingSpot> getAllAvailableSpots() {
        return floors.stream()
                .flatMap(floor -> floor.getAvailableSpots().stream())
                .collect(Collectors.toList());
    }

    /**
     * Get available spots of a specific size across all floors.
     */
    public List<ParkingSpot> getAvailableSpotsBySize(SpotSize size) {
        return floors.stream()
                .flatMap(floor -> floor.getAvailableSpotsBySize(size).stream())
                .collect(Collectors.toList());
    }

    /**
     * Get total capacity of the parking lot.
     */
    public int getTotalCapacity() {
        return floors.stream()
                .mapToInt(ParkingFloor::getTotalSpots)
                .sum();
    }

    /**
     * Get total available spots.
     */
    public int getTotalAvailableSpots() {
        return floors.stream()
                .mapToInt(ParkingFloor::getAvailableSpotCount)
                .sum();
    }

    /**
     * Check if parking lot is full.
     */
    public boolean isFull() {
        return getTotalAvailableSpots() == 0;
    }

    @Override
    public String toString() {
        return "ParkingLot{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", floors=" + floors.size() +
                ", totalCapacity=" + getTotalCapacity() +
                ", availableSpots=" + getTotalAvailableSpots() +
                '}';
    }
}

