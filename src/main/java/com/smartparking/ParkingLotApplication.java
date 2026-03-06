package com.smartparking;

import com.smartparking.enums.SpotSize;
import com.smartparking.enums.VehicleType;
import com.smartparking.model.*;
import com.smartparking.repository.*;
import com.smartparking.repository.impl.*;
import com.smartparking.service.FeeCalculationService;
import com.smartparking.service.ParkingService;
import com.smartparking.strategy.impl.FirstAvailableStrategy;

/**
 * Main application class for the Smart Parking System.
 * Demonstrates the complete parking lot management system.
 */
public class ParkingLotApplication {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("SMART PARKING LOT MANAGEMENT SYSTEM");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Initialize the parking lot
        ParkingLot parkingLot = initializeParkingLot();
        
        // Initialize repositories
        VehicleRepository vehicleRepository = new InMemoryVehicleRepository();
        ParkingSpotRepository spotRepository = new InMemoryParkingSpotRepository();
        TransactionRepository transactionRepository = new InMemoryTransactionRepository();
        
        // Load parking spots into repository
        parkingLot.getAllParkingSpots().forEach(spotRepository::save);
        
        // Initialize services
        FeeCalculationService feeService = new FeeCalculationService();
        ParkingService parkingService = new ParkingService(
            vehicleRepository,
            spotRepository,
            transactionRepository,
            feeService,
            new FirstAvailableStrategy()
        );
        
        System.out.println("Parking Lot Initialized:");
        System.out.println(parkingLot);
        System.out.println();
        
        // Run demo scenarios
        runDemoScenarios(parkingService);
    }
    
    /**
     * Initialize a multi-floor parking lot with various spot sizes.
     */
    private static ParkingLot initializeParkingLot() {
        ParkingLot parkingLot = new ParkingLot(
            "PL001",
            "Downtown Smart Parking",
            "123 Main Street, City Center"
        );
        
        // Create 3 floors
        for (int floor = 1; floor <= 3; floor++) {
            ParkingFloor parkingFloor = new ParkingFloor(floor);
            
            // Add spots to each floor
            // Floor 1: Mixed spots
            if (floor == 1) {
                for (int i = 1; i <= 5; i++) {
                    parkingFloor.addParkingSpot(new ParkingSpot(
                        "F1-S" + i, floor, SpotSize.SMALL, 10 + i * 5
                    ));
                }
                for (int i = 6; i <= 15; i++) {
                    parkingFloor.addParkingSpot(new ParkingSpot(
                        "F1-M" + i, floor, SpotSize.MEDIUM, 10 + i * 5
                    ));
                }
                for (int i = 16; i <= 20; i++) {
                    parkingFloor.addParkingSpot(new ParkingSpot(
                        "F1-L" + i, floor, SpotSize.LARGE, 10 + i * 5
                    ));
                }
            }
            // Floor 2: Mostly medium spots
            else if (floor == 2) {
                for (int i = 1; i <= 3; i++) {
                    parkingFloor.addParkingSpot(new ParkingSpot(
                        "F2-S" + i, floor, SpotSize.SMALL, 20 + i * 5
                    ));
                }
                for (int i = 4; i <= 18; i++) {
                    parkingFloor.addParkingSpot(new ParkingSpot(
                        "F2-M" + i, floor, SpotSize.MEDIUM, 20 + i * 5
                    ));
                }
            }
            // Floor 3: Large spots for buses and trucks
            else {
                for (int i = 1; i <= 10; i++) {
                    parkingFloor.addParkingSpot(new ParkingSpot(
                        "F3-L" + i, floor, SpotSize.LARGE, 30 + i * 5
                    ));
                }
            }
            
            parkingLot.addFloor(parkingFloor);
        }
        
        return parkingLot;
    }
    
    /**
     * Run various demo scenarios to showcase the system.
     */
    private static void runDemoScenarios(ParkingService parkingService) {
        System.out.println("SCENARIO 1: Multiple Vehicle Check-ins");
        System.out.println("-".repeat(80));
        
        // Create vehicles
        Vehicle car1 = new Vehicle("ABC123", VehicleType.CAR, "John Doe", "555-0001");
        Vehicle car2 = new Vehicle("XYZ789", VehicleType.CAR, "Jane Smith", "555-0002");
        Vehicle motorcycle1 = new Vehicle("MOTO01", VehicleType.MOTORCYCLE, "Bob Wilson", "555-0003");
        Vehicle bus1 = new Vehicle("BUS001", VehicleType.BUS, "Transit Co", "555-0004");
        
        // Check in vehicles
        try {
            ParkingTransaction t1 = parkingService.checkIn(car1);
            ParkingTransaction t2 = parkingService.checkIn(car2);
            ParkingTransaction t3 = parkingService.checkIn(motorcycle1);
            ParkingTransaction t4 = parkingService.checkIn(bus1);
            
            System.out.println();
            System.out.println("Current Status: " + parkingService.getParkingStatus());
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Simulate some time passing and check out
        System.out.println("SCENARIO 2: Vehicle Check-outs and Fee Calculation");
        System.out.println("-".repeat(80));
        
        try {
            Thread.sleep(2000); // Simulate 2 seconds (in real scenario, this would be hours)
            
            double fee1 = parkingService.checkOut("ABC123");
            double fee2 = parkingService.checkOut("MOTO01");
            
            System.out.println();
            System.out.println("Current Status: " + parkingService.getParkingStatus());
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Test concurrent operations
        System.out.println("SCENARIO 3: Concurrent Vehicle Operations");
        System.out.println("-".repeat(80));

        testConcurrentOperations(parkingService);

        System.out.println();
        System.out.println("SCENARIO 4: Error Handling - Duplicate Check-in");
        System.out.println("-".repeat(80));

        try {
            // Try to check in a vehicle that's already parked
            parkingService.checkIn(car2); // car2 is still parked
        } catch (Exception e) {
            System.out.println("✓ Expected error caught: " + e.getMessage());
        }

        System.out.println();
        System.out.println("SCENARIO 5: Final Status Report");
        System.out.println("-".repeat(80));
        System.out.println("Final Parking Status: " + parkingService.getParkingStatus());

        System.out.println();
        System.out.println("=".repeat(80));
        System.out.println("DEMO COMPLETED SUCCESSFULLY");
        System.out.println("=".repeat(80));
    }

    /**
     * Test concurrent operations to demonstrate thread safety.
     */
    private static void testConcurrentOperations(ParkingService parkingService) {
        Thread[] threads = new Thread[5];

        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    Vehicle vehicle = new Vehicle(
                        "CONCURRENT" + index,
                        VehicleType.CAR,
                        "Driver " + index,
                        "555-100" + index
                    );
                    parkingService.checkIn(vehicle);
                } catch (Exception e) {
                    System.err.println("Thread " + index + " error: " + e.getMessage());
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("✓ All concurrent operations completed");
        System.out.println("Status after concurrent operations: " + parkingService.getParkingStatus());
    }
}
