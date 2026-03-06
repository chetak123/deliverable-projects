package com.smartparking.strategy;

import com.smartparking.enums.SpotSize;
import com.smartparking.enums.VehicleType;
import com.smartparking.model.ParkingSpot;
import com.smartparking.model.Vehicle;
import com.smartparking.strategy.impl.BestFitStrategy;
import com.smartparking.strategy.impl.FirstAvailableStrategy;
import com.smartparking.strategy.impl.NearestToEntranceStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for allocation strategies.
 */
class AllocationStrategyTest {

    private List<ParkingSpot> availableSpots;

    @BeforeEach
    void setUp() {
        availableSpots = new ArrayList<>();
        availableSpots.add(new ParkingSpot("F1-S1", 1, SpotSize.SMALL, 50));
        availableSpots.add(new ParkingSpot("F1-M1", 1, SpotSize.MEDIUM, 30));
        availableSpots.add(new ParkingSpot("F1-M2", 1, SpotSize.MEDIUM, 20));
        availableSpots.add(new ParkingSpot("F1-L1", 1, SpotSize.LARGE, 40));
    }

    @Test
    void testFirstAvailableStrategy() {
        FirstAvailableStrategy strategy = new FirstAvailableStrategy();
        Vehicle car = new Vehicle("C001", VehicleType.CAR, "John", "555-0001");
        
        Optional<ParkingSpot> spot = strategy.allocateSpot(car, availableSpots);
        
        assertTrue(spot.isPresent());
        // Should return first suitable spot (MEDIUM or larger)
        assertTrue(spot.get().getSpotSize() == SpotSize.MEDIUM || 
                   spot.get().getSpotSize() == SpotSize.LARGE);
    }

    @Test
    void testNearestToEntranceStrategy() {
        NearestToEntranceStrategy strategy = new NearestToEntranceStrategy();
        Vehicle car = new Vehicle("C001", VehicleType.CAR, "John", "555-0001");
        
        Optional<ParkingSpot> spot = strategy.allocateSpot(car, availableSpots);
        
        assertTrue(spot.isPresent());
        // Should return the nearest suitable spot (F1-M2 with distance 20)
        assertEquals("F1-M2", spot.get().getSpotId());
    }

    @Test
    void testBestFitStrategy() {
        BestFitStrategy strategy = new BestFitStrategy();
        Vehicle car = new Vehicle("C001", VehicleType.CAR, "John", "555-0001");
        
        Optional<ParkingSpot> spot = strategy.allocateSpot(car, availableSpots);
        
        assertTrue(spot.isPresent());
        // Should return MEDIUM spot (best fit for CAR)
        assertEquals(SpotSize.MEDIUM, spot.get().getSpotSize());
    }

    @Test
    void testMotorcycleAllocation() {
        FirstAvailableStrategy strategy = new FirstAvailableStrategy();
        Vehicle motorcycle = new Vehicle("M001", VehicleType.MOTORCYCLE, "Alice", "555-1111");
        
        Optional<ParkingSpot> spot = strategy.allocateSpot(motorcycle, availableSpots);
        
        assertTrue(spot.isPresent());
        // Motorcycle can fit in any spot
    }

    @Test
    void testBusAllocation() {
        FirstAvailableStrategy strategy = new FirstAvailableStrategy();
        Vehicle bus = new Vehicle("B001", VehicleType.BUS, "Charlie", "555-3333");
        
        Optional<ParkingSpot> spot = strategy.allocateSpot(bus, availableSpots);
        
        assertTrue(spot.isPresent());
        // Bus needs LARGE spot
        assertEquals(SpotSize.LARGE, spot.get().getSpotSize());
    }

    @Test
    void testNoSuitableSpot() {
        FirstAvailableStrategy strategy = new FirstAvailableStrategy();
        Vehicle bus = new Vehicle("B001", VehicleType.BUS, "Charlie", "555-3333");
        
        // Remove large spot
        availableSpots.removeIf(spot -> spot.getSpotSize() == SpotSize.LARGE);
        
        Optional<ParkingSpot> spot = strategy.allocateSpot(bus, availableSpots);
        
        assertFalse(spot.isPresent());
    }

    @Test
    void testEmptySpotList() {
        FirstAvailableStrategy strategy = new FirstAvailableStrategy();
        Vehicle car = new Vehicle("C001", VehicleType.CAR, "John", "555-0001");
        
        Optional<ParkingSpot> spot = strategy.allocateSpot(car, new ArrayList<>());
        
        assertFalse(spot.isPresent());
    }

    @Test
    void testBestFitPrefersSmallestSuitableSpot() {
        BestFitStrategy strategy = new BestFitStrategy();
        Vehicle motorcycle = new Vehicle("M001", VehicleType.MOTORCYCLE, "Alice", "555-1111");
        
        Optional<ParkingSpot> spot = strategy.allocateSpot(motorcycle, availableSpots);
        
        assertTrue(spot.isPresent());
        // Should prefer SMALL spot for motorcycle
        assertEquals(SpotSize.SMALL, spot.get().getSpotSize());
    }

    @Test
    void testNearestToEntranceWithMultipleSameSizeSpots() {
        NearestToEntranceStrategy strategy = new NearestToEntranceStrategy();
        Vehicle car = new Vehicle("C001", VehicleType.CAR, "John", "555-0001");
        
        Optional<ParkingSpot> spot = strategy.allocateSpot(car, availableSpots);
        
        assertTrue(spot.isPresent());
        // Should return the nearest MEDIUM spot (F1-M2 with distance 20)
        assertEquals(20, spot.get().getDistanceFromEntrance());
    }
}

