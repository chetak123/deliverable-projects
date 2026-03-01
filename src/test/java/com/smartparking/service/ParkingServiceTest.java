package com.smartparking.service;

import com.smartparking.enums.SpotSize;
import com.smartparking.enums.VehicleType;
import com.smartparking.model.ParkingSpot;
import com.smartparking.model.ParkingTransaction;
import com.smartparking.model.Vehicle;
import com.smartparking.repository.ParkingSpotRepository;
import com.smartparking.repository.TransactionRepository;
import com.smartparking.repository.VehicleRepository;
import com.smartparking.repository.impl.InMemoryParkingSpotRepository;
import com.smartparking.repository.impl.InMemoryTransactionRepository;
import com.smartparking.repository.impl.InMemoryVehicleRepository;
import com.smartparking.strategy.impl.FirstAvailableStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParkingService.
 */
class ParkingServiceTest {

    private ParkingService parkingService;
    private VehicleRepository vehicleRepository;
    private ParkingSpotRepository spotRepository;
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        vehicleRepository = new InMemoryVehicleRepository();
        spotRepository = new InMemoryParkingSpotRepository();
        transactionRepository = new InMemoryTransactionRepository();
        FeeCalculationService feeService = new FeeCalculationService();
        
        parkingService = new ParkingService(
            vehicleRepository,
            spotRepository,
            transactionRepository,
            feeService,
            new FirstAvailableStrategy()
        );
        
        // Add some parking spots
        spotRepository.save(new ParkingSpot("F1-S1", 1, SpotSize.SMALL, 10));
        spotRepository.save(new ParkingSpot("F1-M1", 1, SpotSize.MEDIUM, 20));
        spotRepository.save(new ParkingSpot("F1-L1", 1, SpotSize.LARGE, 30));
    }

    @Test
    void testCheckInSuccess() {
        Vehicle vehicle = new Vehicle("ABC123", VehicleType.CAR, "John Doe", "555-0001");
        
        ParkingTransaction transaction = parkingService.checkIn(vehicle);
        
        assertNotNull(transaction);
        assertEquals("ABC123", transaction.getVehicleLicensePlate());
        assertNotNull(transaction.getSpotId());
        assertNotNull(transaction.getEntryTime());
    }

    @Test
    void testCheckInDuplicateVehicle() {
        Vehicle vehicle = new Vehicle("ABC123", VehicleType.CAR, "John Doe", "555-0001");
        
        parkingService.checkIn(vehicle);
        
        assertThrows(IllegalStateException.class, () -> {
            parkingService.checkIn(vehicle);
        });
    }

    @Test
    void testCheckInNoAvailableSpots() {
        // Occupy all 3 spots (SMALL, MEDIUM, LARGE)
        // MOTORCYCLE goes to SMALL spot
        parkingService.checkIn(new Vehicle("V1", VehicleType.MOTORCYCLE, "A", "1"));
        // First CAR goes to MEDIUM spot
        parkingService.checkIn(new Vehicle("V2", VehicleType.CAR, "B", "2"));
        // Second CAR goes to LARGE spot (since MEDIUM is occupied)
        parkingService.checkIn(new Vehicle("V3", VehicleType.CAR, "C", "3"));

        // Try to check in another vehicle - should fail as all spots are occupied
        Vehicle vehicle = new Vehicle("V4", VehicleType.CAR, "D", "4");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            parkingService.checkIn(vehicle);
        });

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("No parking spots available"),
            "Expected message to contain 'No parking spots available' but was: " + exception.getMessage());
    }

    @Test
    void testCheckOutSuccess() {
        Vehicle vehicle = new Vehicle("ABC123", VehicleType.CAR, "John Doe", "555-0001");
        parkingService.checkIn(vehicle);
        
        double fee = parkingService.checkOut("ABC123");
        
        assertTrue(fee >= 0);
    }

    @Test
    void testCheckOutNonExistentVehicle() {
        assertThrows(IllegalStateException.class, () -> {
            parkingService.checkOut("NONEXISTENT");
        });
    }

    @Test
    void testGetParkingStatus() {
        parkingService.checkIn(new Vehicle("V1", VehicleType.CAR, "A", "1"));
        
        ParkingService.ParkingStatus status = parkingService.getParkingStatus();
        
        assertEquals(3, status.getTotalSpots());
        assertEquals(1, status.getOccupiedSpots());
        assertEquals(2, status.getAvailableSpots());
    }

    @Test
    void testGetActiveTransaction() {
        Vehicle vehicle = new Vehicle("ABC123", VehicleType.CAR, "John Doe", "555-0001");
        ParkingTransaction transaction = parkingService.checkIn(vehicle);
        
        var activeTransaction = parkingService.getActiveTransaction("ABC123");
        
        assertTrue(activeTransaction.isPresent());
        assertEquals(transaction.getTransactionId(), activeTransaction.get().getTransactionId());
    }

    @Test
    void testConcurrentCheckIns() throws InterruptedException {
        // Add more spots to ensure we have enough for concurrent operations
        spotRepository.save(new ParkingSpot("F1-M2", 1, SpotSize.MEDIUM, 21));
        spotRepository.save(new ParkingSpot("F1-M3", 1, SpotSize.MEDIUM, 22));

        final int threadCount = 3;
        Thread[] threads = new Thread[threadCount];
        Exception[] exceptions = new Exception[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    Vehicle vehicle = new Vehicle("V" + index, VehicleType.CAR, "Owner" + index, "555-000" + index);
                    parkingService.checkIn(vehicle);
                } catch (Exception e) {
                    exceptions[index] = e;
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Check that no exceptions occurred
        for (int i = 0; i < threadCount; i++) {
            assertNull(exceptions[i], "Thread " + i + " should not have thrown an exception");
        }

        // All 3 vehicles should be checked in successfully
        ParkingService.ParkingStatus status = parkingService.getParkingStatus();
        assertEquals(3, status.getOccupiedSpots());
    }
}

