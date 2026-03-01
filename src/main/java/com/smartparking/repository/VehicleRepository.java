package com.smartparking.repository;

import com.smartparking.model.Vehicle;

import java.util.Optional;

/**
 * Repository interface for Vehicle operations.
 */
public interface VehicleRepository {
    
    /**
     * Save a vehicle to the repository.
     */
    void save(Vehicle vehicle);
    
    /**
     * Find a vehicle by license plate.
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    /**
     * Check if a vehicle exists.
     */
    boolean exists(String licensePlate);
    
    /**
     * Delete a vehicle.
     */
    void delete(String licensePlate);
}

