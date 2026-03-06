package com.smartparking.repository.impl;

import com.smartparking.model.Vehicle;
import com.smartparking.repository.VehicleRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of VehicleRepository.
 * Thread-safe using ConcurrentHashMap.
 */
public class InMemoryVehicleRepository implements VehicleRepository {
    
    private final Map<String, Vehicle> vehicles = new ConcurrentHashMap<>();
    
    @Override
    public void save(Vehicle vehicle) {
        vehicles.put(vehicle.getLicensePlate(), vehicle);
    }
    
    @Override
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        return Optional.ofNullable(vehicles.get(licensePlate.toUpperCase()));
    }
    
    @Override
    public boolean exists(String licensePlate) {
        return vehicles.containsKey(licensePlate.toUpperCase());
    }
    
    @Override
    public void delete(String licensePlate) {
        vehicles.remove(licensePlate.toUpperCase());
    }
}

