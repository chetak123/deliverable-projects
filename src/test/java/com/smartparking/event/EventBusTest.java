package com.smartparking.event;

import com.smartparking.enums.SpotSize;
import com.smartparking.enums.VehicleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EventBus.
 */
class EventBusTest {

    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        eventBus = EventBus.getInstance();
        eventBus.clearHistory();
    }

    @AfterEach
    void tearDown() {
        eventBus.clearHistory();
    }

    @Test
    void testPublishAndSubscribeVehicleEntryEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        
        eventBus.subscribe(VehicleEntryEvent.class, event -> {
            eventCount.incrementAndGet();
            latch.countDown();
        });
        
        VehicleEntryEvent event = new VehicleEntryEvent(
            "ABC123", VehicleType.CAR, "F1-M1", "TXN001"
        );
        eventBus.publish(event);
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, eventCount.get());
    }

    @Test
    void testPublishAndSubscribeVehicleExitEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        
        eventBus.subscribe(VehicleExitEvent.class, event -> {
            eventCount.incrementAndGet();
            assertEquals(25.0, event.getFee());
            assertEquals(120, event.getDurationMinutes());
            latch.countDown();
        });
        
        VehicleExitEvent event = new VehicleExitEvent(
            "ABC123", VehicleType.CAR, "F1-M1", "TXN001", 25.0, 120
        );
        eventBus.publish(event);
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, eventCount.get());
    }

    @Test
    void testPublishAndSubscribeSpotAvailabilityEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        
        eventBus.subscribe(SpotAvailabilityChangedEvent.class, event -> {
            eventCount.incrementAndGet();
            assertEquals("F1-M1", event.getSpotId());
            assertFalse(event.isNowAvailable());
            latch.countDown();
        });
        
        SpotAvailabilityChangedEvent event = new SpotAvailabilityChangedEvent(
            "F1-M1", SpotSize.MEDIUM, 1, false
        );
        eventBus.publish(event);
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, eventCount.get());
    }

    @Test
    void testMultipleSubscribers() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger eventCount = new AtomicInteger(0);
        
        // Subscribe 3 listeners
        for (int i = 0; i < 3; i++) {
            eventBus.subscribe(VehicleEntryEvent.class, event -> {
                eventCount.incrementAndGet();
                latch.countDown();
            });
        }
        
        VehicleEntryEvent event = new VehicleEntryEvent(
            "ABC123", VehicleType.CAR, "F1-M1", "TXN001"
        );
        eventBus.publish(event);
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(3, eventCount.get());
    }

    @Test
    void testEventHistory() {
        VehicleEntryEvent event1 = new VehicleEntryEvent(
            "ABC123", VehicleType.CAR, "F1-M1", "TXN001"
        );
        VehicleExitEvent event2 = new VehicleExitEvent(
            "ABC123", VehicleType.CAR, "F1-M1", "TXN001", 25.0, 120
        );
        
        eventBus.publish(event1);
        eventBus.publish(event2);
        
        var history = eventBus.getEventHistory();
        
        assertEquals(2, history.size());
    }

    @Test
    void testClearHistory() {
        VehicleEntryEvent event = new VehicleEntryEvent(
            "ABC123", VehicleType.CAR, "F1-M1", "TXN001"
        );
        eventBus.publish(event);
        
        assertEquals(1, eventBus.getEventHistory().size());
        
        eventBus.clearHistory();
        
        assertEquals(0, eventBus.getEventHistory().size());
    }

    @Test
    void testPublishSync() {
        AtomicInteger eventCount = new AtomicInteger(0);
        
        eventBus.subscribe(VehicleEntryEvent.class, event -> {
            eventCount.incrementAndGet();
        });
        
        VehicleEntryEvent event = new VehicleEntryEvent(
            "ABC123", VehicleType.CAR, "F1-M1", "TXN001"
        );
        eventBus.publishSync(event);
        
        // With sync publish, event should be processed immediately
        assertEquals(1, eventCount.get());
    }

    @Test
    void testEventWithNoSubscribers() {
        VehicleEntryEvent event = new VehicleEntryEvent(
            "ABC123", VehicleType.CAR, "F1-M1", "TXN001"
        );
        
        // Should not throw exception
        assertDoesNotThrow(() -> eventBus.publish(event));
    }
}

