package com.smartparking.event;

import com.smartparking.enums.VehicleType;

/**
 * Event published when a vehicle exits the parking lot.
 */
public class VehicleExitEvent extends ParkingEvent {
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final String spotId;
    private final String transactionId;
    private final double fee;
    private final long durationMinutes;

    public VehicleExitEvent(String licensePlate, VehicleType vehicleType, String spotId, 
                           String transactionId, double fee, long durationMinutes) {
        super("VEHICLE_EXIT");
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.spotId = spotId;
        this.transactionId = transactionId;
        this.fee = fee;
        this.durationMinutes = durationMinutes;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public String getSpotId() {
        return spotId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getFee() {
        return fee;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    @Override
    public String toString() {
        return "VehicleExitEvent{" +
                "licensePlate='" + licensePlate + '\'' +
                ", vehicleType=" + vehicleType +
                ", spotId='" + spotId + '\'' +
                ", fee=" + fee +
                ", duration=" + durationMinutes + " mins" +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

