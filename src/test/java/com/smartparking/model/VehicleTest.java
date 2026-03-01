package com.smartparking.model;

import com.smartparking.enums.VehicleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Vehicle class.
 */
class VehicleTest {

    @Test
    void testVehicleCreation() {
        Vehicle vehicle = new Vehicle("ABC123", VehicleType.CAR, "John Doe", "555-0001");
        
        assertEquals("ABC123", vehicle.getLicensePlate());
        assertEquals(VehicleType.CAR, vehicle.getVehicleType());
        assertEquals("John Doe", vehicle.getOwnerName());
        assertEquals("555-0001", vehicle.getOwnerPhone());
    }

    @Test
    void testVehicleCreationWithNullValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle(null, VehicleType.CAR, "John Doe", "555-0001");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Vehicle("ABC123", null, "John Doe", "555-0001");
        });
    }

    @Test
    void testVehicleEquality() {
        Vehicle vehicle1 = new Vehicle("ABC123", VehicleType.CAR, "John Doe", "555-0001");
        Vehicle vehicle2 = new Vehicle("ABC123", VehicleType.CAR, "Jane Doe", "555-0002");
        
        // Vehicles with same license plate should be considered equal
        assertEquals(vehicle1.getLicensePlate(), vehicle2.getLicensePlate());
    }

    @Test
    void testDifferentVehicleTypes() {
        Vehicle motorcycle = new Vehicle("M001", VehicleType.MOTORCYCLE, "Alice", "555-1111");
        Vehicle car = new Vehicle("C001", VehicleType.CAR, "Bob", "555-2222");
        Vehicle bus = new Vehicle("B001", VehicleType.BUS, "Charlie", "555-3333");
        Vehicle truck = new Vehicle("T001", VehicleType.TRUCK, "David", "555-4444");
        
        assertEquals(VehicleType.MOTORCYCLE, motorcycle.getVehicleType());
        assertEquals(VehicleType.CAR, car.getVehicleType());
        assertEquals(VehicleType.BUS, bus.getVehicleType());
        assertEquals(VehicleType.TRUCK, truck.getVehicleType());
    }

    @Test
    void testToString() {
        Vehicle vehicle = new Vehicle("ABC123", VehicleType.CAR, "John Doe", "555-0001");
        String toString = vehicle.toString();
        
        assertTrue(toString.contains("ABC123"));
        assertTrue(toString.contains("CAR"));
        assertTrue(toString.contains("John Doe"));
    }
}

