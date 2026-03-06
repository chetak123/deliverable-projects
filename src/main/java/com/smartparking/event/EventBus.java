package com.smartparking.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Event bus for publishing and subscribing to parking events.
 * Supports asynchronous event processing.
 */
public class EventBus {
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    private static EventBus instance;
    
    private final Map<Class<? extends ParkingEvent>, List<EventListener<? extends ParkingEvent>>> listeners;
    private final ExecutorService executorService;
    private final List<ParkingEvent> eventHistory;
    
    private EventBus() {
        this.listeners = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(10);
        this.eventHistory = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Get singleton instance of EventBus.
     */
    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }
    
    /**
     * Subscribe to events of a specific type.
     */
    public <T extends ParkingEvent> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        logger.info("Subscribed listener for event type: {}", eventType.getSimpleName());
    }
    
    /**
     * Publish an event to all subscribers.
     */
    @SuppressWarnings("unchecked")
    public <T extends ParkingEvent> void publish(T event) {
        eventHistory.add(event);
        logger.debug("Publishing event: {}", event);
        
        List<EventListener<? extends ParkingEvent>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener<? extends ParkingEvent> listener : eventListeners) {
                executorService.submit(() -> {
                    try {
                        ((EventListener<T>) listener).onEvent(event);
                    } catch (Exception e) {
                        logger.error("Error processing event: {}", event, e);
                    }
                });
            }
        }
    }
    
    /**
     * Publish an event synchronously.
     */
    @SuppressWarnings("unchecked")
    public <T extends ParkingEvent> void publishSync(T event) {
        eventHistory.add(event);
        logger.debug("Publishing event synchronously: {}", event);
        
        List<EventListener<? extends ParkingEvent>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener<? extends ParkingEvent> listener : eventListeners) {
                try {
                    ((EventListener<T>) listener).onEvent(event);
                } catch (Exception e) {
                    logger.error("Error processing event: {}", event, e);
                }
            }
        }
    }
    
    /**
     * Get event history.
     */
    public List<ParkingEvent> getEventHistory() {
        return new ArrayList<>(eventHistory);
    }
    
    /**
     * Clear event history.
     */
    public void clearHistory() {
        eventHistory.clear();
    }
    
    /**
     * Shutdown the event bus.
     */
    public void shutdown() {
        executorService.shutdown();
        logger.info("EventBus shutdown");
    }
}

