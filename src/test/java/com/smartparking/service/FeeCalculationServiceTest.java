package com.smartparking.service;

import com.smartparking.enums.VehicleType;
import com.smartparking.model.ParkingTransaction;
import com.smartparking.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FeeCalculationService.
 */
class FeeCalculationServiceTest {

    private FeeCalculationService feeService;

    @BeforeEach
    void setUp() {
        feeService = new FeeCalculationService();
    }

    @Test
    void testCalculateFeeForMotorcycle1Hour() {
        Vehicle vehicle = new Vehicle("M001", VehicleType.MOTORCYCLE, "Alice", "555-1111");
        ParkingTransaction transaction = new ParkingTransaction("M001", "F1-S1");
        
        // Simulate 1 hour parking
        transaction.setEntryTimeForTesting(LocalDateTime.now().minusHours(1));
        
        double fee = feeService.calculateFee(transaction, vehicle);
        
        assertEquals(2.0, fee, 0.01); // $2/hour for motorcycle
    }

    @Test
    void testCalculateFeeForCar2Hours() {
        Vehicle vehicle = new Vehicle("C001", VehicleType.CAR, "Bob", "555-2222");
        ParkingTransaction transaction = new ParkingTransaction("C001", "F1-M1");
        
        // Simulate 2 hours parking
        transaction.setEntryTimeForTesting(LocalDateTime.now().minusHours(2));
        
        double fee = feeService.calculateFee(transaction, vehicle);
        
        assertEquals(10.0, fee, 0.01); // $5/hour for car
    }

    @Test
    void testCalculateFeeForBus3Hours() {
        Vehicle vehicle = new Vehicle("B001", VehicleType.BUS, "Charlie", "555-3333");
        ParkingTransaction transaction = new ParkingTransaction("B001", "F1-L1");
        
        // Simulate 3 hours parking
        transaction.setEntryTimeForTesting(LocalDateTime.now().minusHours(3));
        
        double fee = feeService.calculateFee(transaction, vehicle);
        
        assertEquals(30.0, fee, 0.01); // $10/hour for bus
    }

    @Test
    void testCalculateFeeForTruck4Hours() {
        Vehicle vehicle = new Vehicle("T001", VehicleType.TRUCK, "David", "555-4444");
        ParkingTransaction transaction = new ParkingTransaction("T001", "F1-L2");
        
        // Simulate 4 hours parking
        transaction.setEntryTimeForTesting(LocalDateTime.now().minusHours(4));
        
        double fee = feeService.calculateFee(transaction, vehicle);
        
        assertEquals(32.0, fee, 0.01); // $8/hour for truck
    }

    @Test
    void testCalculateFeeWithDailyCap() {
        Vehicle vehicle = new Vehicle("C001", VehicleType.CAR, "Bob", "555-2222");
        ParkingTransaction transaction = new ParkingTransaction("C001", "F1-M1");
        
        // Simulate 24 hours parking (should hit daily cap)
        transaction.setEntryTimeForTesting(LocalDateTime.now().minusHours(24));
        
        double fee = feeService.calculateFee(transaction, vehicle);
        
        assertEquals(50.0, fee, 0.01); // Daily cap for car is $50
    }

    @Test
    void testCalculateFeeForPartialHour() {
        Vehicle vehicle = new Vehicle("C001", VehicleType.CAR, "Bob", "555-2222");
        ParkingTransaction transaction = new ParkingTransaction("C001", "F1-M1");
        
        // Simulate 30 minutes parking (should round up to 1 hour)
        transaction.setEntryTimeForTesting(LocalDateTime.now().minusMinutes(30));
        
        double fee = feeService.calculateFee(transaction, vehicle);
        
        assertEquals(5.0, fee, 0.01); // Minimum 1 hour charge
    }

    @Test
    void testCalculateFeeForZeroDuration() {
        Vehicle vehicle = new Vehicle("C001", VehicleType.CAR, "Bob", "555-2222");
        ParkingTransaction transaction = new ParkingTransaction("C001", "F1-M1");

        // Entry time is now (0 duration)
        transaction.setEntryTimeForTesting(LocalDateTime.now());

        double fee = feeService.calculateFee(transaction, vehicle);

        // Zero duration results in zero fee (no minimum charge in current implementation)
        assertEquals(0.0, fee, 0.01);
    }

    @Test
    void testEstimateFee() {
        double motorcycleFee = feeService.estimateFee(VehicleType.MOTORCYCLE, 2);
        double carFee = feeService.estimateFee(VehicleType.CAR, 3);
        double busFee = feeService.estimateFee(VehicleType.BUS, 4);
        double truckFee = feeService.estimateFee(VehicleType.TRUCK, 5);
        
        assertEquals(4.0, motorcycleFee, 0.01);
        assertEquals(15.0, carFee, 0.01);
        assertEquals(40.0, busFee, 0.01);
        assertEquals(40.0, truckFee, 0.01);
    }

    @Test
    void testEstimateFeeWithDailyCap() {
        double fee = feeService.estimateFee(VehicleType.CAR, 24);
        
        assertEquals(50.0, fee, 0.01); // Should hit daily cap
    }
}

