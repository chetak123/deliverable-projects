package com.smartparking.repository.impl;

import com.smartparking.enums.SpotSize;
import com.smartparking.model.ParkingSpot;
import com.smartparking.repository.ParkingSpotRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ParkingSpotRepository.
 * Thread-safe using ConcurrentHashMap.
 */
public class InMemoryParkingSpotRepository implements ParkingSpotRepository {
    
    private final Map<String, ParkingSpot> spots = new ConcurrentHashMap<>();
    
    @Override
    public void save(ParkingSpot spot) {
        spots.put(spot.getSpotId(), spot);
    }
    
    @Override
    public Optional<ParkingSpot> findById(String spotId) {
        return Optional.ofNullable(spots.get(spotId));
    }
    
    @Override
    public List<ParkingSpot> findAll() {
        return new ArrayList<>(spots.values());
    }
    
    @Override
    public List<ParkingSpot> findAvailableSpots() {
        return spots.values().stream()
                .filter(spot -> !spot.isOccupied())
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ParkingSpot> findAvailableSpotsBySize(SpotSize size) {
        return spots.values().stream()
                .filter(spot -> !spot.isOccupied() && spot.getSpotSize() == size)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ParkingSpot> findByFloor(int floorNumber) {
        return spots.values().stream()
                .filter(spot -> spot.getFloorNumber() == floorNumber)
                .collect(Collectors.toList());
    }
}

