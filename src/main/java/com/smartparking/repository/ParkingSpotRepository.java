package com.smartparking.repository;

import com.smartparking.enums.SpotSize;
import com.smartparking.model.ParkingSpot;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ParkingSpot operations.
 */
public interface ParkingSpotRepository {
    
    /**
     * Save a parking spot.
     */
    void save(ParkingSpot spot);
    
    /**
     * Find a parking spot by ID.
     */
    Optional<ParkingSpot> findById(String spotId);
    
    /**
     * Get all parking spots.
     */
    List<ParkingSpot> findAll();
    
    /**
     * Get all available parking spots.
     */
    List<ParkingSpot> findAvailableSpots();
    
    /**
     * Get available spots by size.
     */
    List<ParkingSpot> findAvailableSpotsBySize(SpotSize size);
    
    /**
     * Get spots by floor number.
     */
    List<ParkingSpot> findByFloor(int floorNumber);
}

