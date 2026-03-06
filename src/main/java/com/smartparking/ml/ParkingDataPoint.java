package com.smartparking.ml;

import com.smartparking.enums.VehicleType;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * Data point for ML training and prediction.
 */
public class ParkingDataPoint {
    private final LocalDateTime timestamp;
    private final int hourOfDay;
    private final DayOfWeek dayOfWeek;
    private final VehicleType vehicleType;
    private final int occupancyRate; // Percentage
    private final long parkingDuration; // Minutes
    
    public ParkingDataPoint(LocalDateTime timestamp, VehicleType vehicleType, 
                           int occupancyRate, long parkingDuration) {
        this.timestamp = timestamp;
        this.hourOfDay = timestamp.getHour();
        this.dayOfWeek = timestamp.getDayOfWeek();
        this.vehicleType = vehicleType;
        this.occupancyRate = occupancyRate;
        this.parkingDuration = parkingDuration;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public int getHourOfDay() {
        return hourOfDay;
    }
    
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }
    
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    
    public int getOccupancyRate() {
        return occupancyRate;
    }
    
    public long getParkingDuration() {
        return parkingDuration;
    }
    
    /**
     * Convert to feature vector for ML.
     */
    public double[] toFeatureVector() {
        return new double[] {
            hourOfDay,
            dayOfWeek.getValue(),
            vehicleType.ordinal(),
            occupancyRate
        };
    }
}

