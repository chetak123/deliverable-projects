package com.smartparking.strategy.impl;

import com.smartparking.enums.SpotSize;
import com.smartparking.enums.VehicleType;
import com.smartparking.model.ParkingSpot;
import com.smartparking.model.Vehicle;
import com.smartparking.strategy.SpotAllocationStrategy;

import java.util.List;
import java.util.Optional;

/**
 * First Available Strategy: Allocates the first available spot that fits the vehicle.
 */
public class FirstAvailableStrategy implements SpotAllocationStrategy {
    
    @Override
    public Optional<ParkingSpot> allocateSpot(Vehicle vehicle, List<ParkingSpot> availableSpots) {
        SpotSize requiredSize = getRequiredSpotSize(vehicle.getVehicleType());
        
        return availableSpots.stream()
                .filter(spot -> canFit(vehicle.getVehicleType(), spot.getSpotSize()))
                .findFirst();
    }
    
    /**
     * Get the minimum required spot size for a vehicle type.
     */
    private SpotSize getRequiredSpotSize(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return SpotSize.SMALL;
            case CAR:
                return SpotSize.MEDIUM;
            case BUS:
            case TRUCK:
                return SpotSize.LARGE;
            default:
                return SpotSize.MEDIUM;
        }
    }
    
    /**
     * Check if a vehicle can fit in a spot of given size.
     */
    private boolean canFit(VehicleType vehicleType, SpotSize spotSize) {
        SpotSize requiredSize = getRequiredSpotSize(vehicleType);
        
        // A vehicle can fit in a spot of equal or larger size
        switch (requiredSize) {
            case SMALL:
                return true; // Motorcycle can fit anywhere
            case MEDIUM:
                return spotSize == SpotSize.MEDIUM || spotSize == SpotSize.LARGE || spotSize == SpotSize.EXTRA_LARGE;
            case LARGE:
                return spotSize == SpotSize.LARGE || spotSize == SpotSize.EXTRA_LARGE;
            case EXTRA_LARGE:
                return spotSize == SpotSize.EXTRA_LARGE;
            default:
                return false;
        }
    }
}

