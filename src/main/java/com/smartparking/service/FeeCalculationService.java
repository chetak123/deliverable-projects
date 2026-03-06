package com.smartparking.service;

import com.smartparking.enums.VehicleType;
import com.smartparking.model.ParkingTransaction;
import com.smartparking.model.Vehicle;

/**
 * Service for calculating parking fees.
 */
public class FeeCalculationService {
    
    // Hourly rates for different vehicle types (in dollars)
    private static final double MOTORCYCLE_HOURLY_RATE = 2.0;
    private static final double CAR_HOURLY_RATE = 5.0;
    private static final double BUS_HOURLY_RATE = 10.0;
    private static final double TRUCK_HOURLY_RATE = 8.0;
    
    // Maximum daily rates
    private static final double MOTORCYCLE_MAX_DAILY_RATE = 20.0;
    private static final double CAR_MAX_DAILY_RATE = 50.0;
    private static final double BUS_MAX_DAILY_RATE = 100.0;
    private static final double TRUCK_MAX_DAILY_RATE = 80.0;
    
    /**
     * Calculate the parking fee for a transaction.
     * 
     * @param transaction The parking transaction
     * @param vehicle The vehicle
     * @return The calculated fee
     */
    public double calculateFee(ParkingTransaction transaction, Vehicle vehicle) {
        long hours = transaction.getDurationInHours();
        double hourlyRate = getHourlyRate(vehicle.getVehicleType());
        double maxDailyRate = getMaxDailyRate(vehicle.getVehicleType());
        
        double calculatedFee = hours * hourlyRate;
        
        // Apply daily maximum cap
        return Math.min(calculatedFee, maxDailyRate);
    }
    
    /**
     * Get hourly rate for a vehicle type.
     */
    private double getHourlyRate(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return MOTORCYCLE_HOURLY_RATE;
            case CAR:
                return CAR_HOURLY_RATE;
            case BUS:
                return BUS_HOURLY_RATE;
            case TRUCK:
                return TRUCK_HOURLY_RATE;
            default:
                return CAR_HOURLY_RATE;
        }
    }
    
    /**
     * Get maximum daily rate for a vehicle type.
     */
    private double getMaxDailyRate(VehicleType vehicleType) {
        switch (vehicleType) {
            case MOTORCYCLE:
                return MOTORCYCLE_MAX_DAILY_RATE;
            case CAR:
                return CAR_MAX_DAILY_RATE;
            case BUS:
                return BUS_MAX_DAILY_RATE;
            case TRUCK:
                return TRUCK_MAX_DAILY_RATE;
            default:
                return CAR_MAX_DAILY_RATE;
        }
    }
    
    /**
     * Get a fee estimate for a vehicle type and duration.
     */
    public double estimateFee(VehicleType vehicleType, long hours) {
        double hourlyRate = getHourlyRate(vehicleType);
        double maxDailyRate = getMaxDailyRate(vehicleType);
        
        double calculatedFee = hours * hourlyRate;
        return Math.min(calculatedFee, maxDailyRate);
    }
}

