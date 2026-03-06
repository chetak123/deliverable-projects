package com.smartparking.strategy;

import com.smartparking.model.ParkingSpot;
import com.smartparking.model.Vehicle;

import java.util.List;
import java.util.Optional;

/**
 * Strategy interface for parking spot allocation.
 */
public interface SpotAllocationStrategy {
    
    /**
     * Allocate a parking spot for the given vehicle from available spots.
     * 
     * @param vehicle The vehicle that needs a parking spot
     * @param availableSpots List of available parking spots
     * @return Optional containing the allocated spot, or empty if no suitable spot found
     */
    Optional<ParkingSpot> allocateSpot(Vehicle vehicle, List<ParkingSpot> availableSpots);
}

