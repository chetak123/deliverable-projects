package com.smartparking.model;

import com.smartparking.enums.SpotSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ParkingSpot class.
 */
class ParkingSpotTest {

    private ParkingSpot spot;

    @BeforeEach
    void setUp() {
        spot = new ParkingSpot("F1-S1", 1, SpotSize.MEDIUM, 10);
    }

    @Test
    void testParkingSpotCreation() {
        assertEquals("F1-S1", spot.getSpotId());
        assertEquals(1, spot.getFloorNumber());
        assertEquals(SpotSize.MEDIUM, spot.getSpotSize());
        assertEquals(10, spot.getDistanceFromEntrance());
        assertFalse(spot.isOccupied());
        assertNull(spot.getCurrentVehicleLicensePlate());
    }

    @Test
    void testOccupySpot() {
        boolean result = spot.occupy("ABC123");
        
        assertTrue(result);
        assertTrue(spot.isOccupied());
        assertEquals("ABC123", spot.getCurrentVehicleLicensePlate());
    }

    @Test
    void testOccupyAlreadyOccupiedSpot() {
        spot.occupy("ABC123");
        boolean result = spot.occupy("XYZ789");
        
        assertFalse(result);
        assertTrue(spot.isOccupied());
        assertEquals("ABC123", spot.getCurrentVehicleLicensePlate());
    }

    @Test
    void testReleaseSpot() {
        spot.occupy("ABC123");
        spot.release();
        
        assertFalse(spot.isOccupied());
        assertNull(spot.getCurrentVehicleLicensePlate());
    }

    @Test
    void testReleaseUnoccupiedSpot() {
        assertDoesNotThrow(() -> spot.release());
        assertFalse(spot.isOccupied());
    }

    @Test
    void testVersionIncrementsOnOccupy() {
        long initialVersion = spot.getVersion();
        spot.occupy("ABC123");
        
        assertTrue(spot.getVersion() > initialVersion);
    }

    @Test
    void testVersionIncrementsOnRelease() {
        spot.occupy("ABC123");
        long versionAfterOccupy = spot.getVersion();
        spot.release();

        assertTrue(spot.getVersion() > versionAfterOccupy);
    }

    @Test
    void testConcurrentOccupyAttempts() throws InterruptedException {
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = spot.occupy("VEHICLE-" + index);
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Only one thread should have successfully occupied the spot
        int successCount = 0;
        for (boolean result : results) {
            if (result) successCount++;
        }
        
        assertEquals(1, successCount);
        assertTrue(spot.isOccupied());
    }

    @Test
    void testToString() {
        String toString = spot.toString();

        assertTrue(toString.contains("F1-S1"));
        assertTrue(toString.contains("MEDIUM"));
        assertTrue(toString.contains("occupied=false"));
    }

    @Test
    void testToStringWhenOccupied() {
        spot.occupy("ABC123");
        String toString = spot.toString();
        
        assertTrue(toString.contains("F1-S1"));
        assertTrue(toString.contains("occupied"));
        assertTrue(toString.contains("ABC123"));
    }
}

