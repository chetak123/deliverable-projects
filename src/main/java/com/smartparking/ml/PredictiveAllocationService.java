package com.smartparking.ml;

import com.smartparking.enums.VehicleType;
import com.smartparking.event.EventBus;
import com.smartparking.event.VehicleEntryEvent;
import com.smartparking.event.VehicleExitEvent;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ML-based service for predictive parking allocation.
 * Uses historical data to predict parking patterns and optimize allocation.
 */
public class PredictiveAllocationService {
    private static final Logger logger = LoggerFactory.getLogger(PredictiveAllocationService.class);
    
    private final List<ParkingDataPoint> trainingData;
    private final Map<Integer, DescriptiveStatistics> hourlyOccupancyStats;
    private final Map<DayOfWeek, DescriptiveStatistics> dailyOccupancyStats;
    private final Map<VehicleType, DescriptiveStatistics> vehicleDurationStats;
    
    public PredictiveAllocationService() {
        this.trainingData = new ArrayList<>();
        this.hourlyOccupancyStats = new ConcurrentHashMap<>();
        this.dailyOccupancyStats = new ConcurrentHashMap<>();
        this.vehicleDurationStats = new ConcurrentHashMap<>();
        
        // Initialize statistics for each hour
        for (int i = 0; i < 24; i++) {
            hourlyOccupancyStats.put(i, new DescriptiveStatistics());
        }
        
        // Initialize statistics for each day
        for (DayOfWeek day : DayOfWeek.values()) {
            dailyOccupancyStats.put(day, new DescriptiveStatistics());
        }
        
        // Initialize statistics for each vehicle type
        for (VehicleType type : VehicleType.values()) {
            vehicleDurationStats.put(type, new DescriptiveStatistics());
        }
        
        // Subscribe to events for learning
        subscribeToEvents();
    }
    
    /**
     * Subscribe to parking events to collect training data.
     */
    private void subscribeToEvents() {
        EventBus eventBus = EventBus.getInstance();
        
        eventBus.subscribe(VehicleEntryEvent.class, event -> {
            logger.debug("Learning from entry event: {}", event.getLicensePlate());
        });
        
        eventBus.subscribe(VehicleExitEvent.class, event -> {
            addTrainingData(event);
        });
    }
    
    /**
     * Add training data from exit event.
     */
    private void addTrainingData(VehicleExitEvent event) {
        LocalDateTime timestamp = event.getTimestamp();
        int occupancyRate = 50; // This would come from actual system state
        
        ParkingDataPoint dataPoint = new ParkingDataPoint(
            timestamp,
            event.getVehicleType(),
            occupancyRate,
            event.getDurationMinutes()
        );
        
        trainingData.add(dataPoint);
        
        // Update statistics
        hourlyOccupancyStats.get(dataPoint.getHourOfDay()).addValue(occupancyRate);
        dailyOccupancyStats.get(dataPoint.getDayOfWeek()).addValue(occupancyRate);
        vehicleDurationStats.get(dataPoint.getVehicleType()).addValue(dataPoint.getParkingDuration());
        
        logger.debug("Added training data point. Total data points: {}", trainingData.size());
    }
    
    /**
     * Predict expected occupancy for a given time.
     */
    public double predictOccupancy(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        DayOfWeek day = dateTime.getDayOfWeek();
        
        DescriptiveStatistics hourStats = hourlyOccupancyStats.get(hour);
        DescriptiveStatistics dayStats = dailyOccupancyStats.get(day);
        
        if (hourStats.getN() == 0 || dayStats.getN() == 0) {
            return 50.0; // Default prediction
        }
        
        // Weighted average of hour and day predictions
        double hourPrediction = hourStats.getMean();
        double dayPrediction = dayStats.getMean();
        
        return (hourPrediction * 0.7 + dayPrediction * 0.3);
    }
    
    /**
     * Predict expected parking duration for a vehicle type.
     */
    public long predictParkingDuration(VehicleType vehicleType) {
        DescriptiveStatistics stats = vehicleDurationStats.get(vehicleType);
        
        if (stats.getN() == 0) {
            // Default predictions based on vehicle type
            switch (vehicleType) {
                case MOTORCYCLE: return 60; // 1 hour
                case CAR: return 120; // 2 hours
                case BUS: return 180; // 3 hours
                case TRUCK: return 240; // 4 hours
                default: return 120;
            }
        }
        
        return (long) stats.getMean();
    }
    
    /**
     * Get peak hours based on historical data.
     */
    public List<Integer> getPeakHours() {
        List<Integer> peakHours = new ArrayList<>();
        double threshold = 70.0; // 70% occupancy threshold
        
        for (int hour = 0; hour < 24; hour++) {
            DescriptiveStatistics stats = hourlyOccupancyStats.get(hour);
            if (stats.getN() > 0 && stats.getMean() > threshold) {
                peakHours.add(hour);
            }
        }
        
        return peakHours;
    }
    
    /**
     * Get recommendations for spot allocation based on predictions.
     */
    public AllocationRecommendation getRecommendation(VehicleType vehicleType, LocalDateTime arrivalTime) {
        double predictedOccupancy = predictOccupancy(arrivalTime);
        long predictedDuration = predictParkingDuration(vehicleType);
        
        String strategy;
        if (predictedOccupancy > 80) {
            strategy = "BEST_FIT"; // Optimize space during high occupancy
        } else if (predictedOccupancy > 50) {
            strategy = "NEAREST_TO_ENTRANCE"; // Balance convenience and efficiency
        } else {
            strategy = "FIRST_AVAILABLE"; // Fast allocation during low occupancy
        }
        
        return new AllocationRecommendation(strategy, predictedOccupancy, predictedDuration);
    }
    
    /**
     * Get training data statistics.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDataPoints", trainingData.size());
        stats.put("peakHours", getPeakHours());
        
        Map<String, Double> avgDurations = new HashMap<>();
        for (VehicleType type : VehicleType.values()) {
            DescriptiveStatistics durationStats = vehicleDurationStats.get(type);
            if (durationStats.getN() > 0) {
                avgDurations.put(type.name(), durationStats.getMean());
            }
        }
        stats.put("averageDurations", avgDurations);
        
        return stats;
    }
    
    /**
     * Recommendation for spot allocation.
     */
    public static class AllocationRecommendation {
        private final String recommendedStrategy;
        private final double predictedOccupancy;
        private final long predictedDuration;
        
        public AllocationRecommendation(String recommendedStrategy, double predictedOccupancy, long predictedDuration) {
            this.recommendedStrategy = recommendedStrategy;
            this.predictedOccupancy = predictedOccupancy;
            this.predictedDuration = predictedDuration;
        }
        
        public String getRecommendedStrategy() {
            return recommendedStrategy;
        }
        
        public double getPredictedOccupancy() {
            return predictedOccupancy;
        }
        
        public long getPredictedDuration() {
            return predictedDuration;
        }
        
        @Override
        public String toString() {
            return "AllocationRecommendation{" +
                    "strategy='" + recommendedStrategy + '\'' +
                    ", predictedOccupancy=" + String.format("%.1f%%", predictedOccupancy) +
                    ", predictedDuration=" + predictedDuration + " mins" +
                    '}';
        }
    }
}

