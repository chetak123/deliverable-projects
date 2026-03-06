package com.smartparking.strategy.impl;

import com.smartparking.enums.SpotSize;
import com.smartparking.enums.VehicleType;
import com.smartparking.model.ParkingSpot;
import com.smartparking.model.Vehicle;
import com.smartparking.strategy.SpotAllocationStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Nearest to Entrance Strategy: Allocates the spot closest to the entrance.
 */
public class NearestToEntranceStrategy implements SpotAllocationStrategy {
    
    @Override
    public Optional<ParkingSpot> allocateSpot(Vehicle vehicle, List<ParkingSpot> availableSpots) {
        return availableSpots.stream()
                .filter(spot -> canFit(vehicle.getVehicleType(), spot.getSpotSize()))
                .min(Comparator.comparingInt(ParkingSpot::getDistanceFromEntrance));
    }
    
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
    
    private boolean canFit(VehicleType vehicleType, SpotSize spotSize) {
        SpotSize requiredSize = getRequiredSpotSize(vehicleType);
        
        switch (requiredSize) {
            case SMALL:
                return true;
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

