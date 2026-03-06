package com.smartparking.event;

import com.smartparking.enums.VehicleType;

/**
 * Event published when a vehicle enters the parking lot.
 */
public class VehicleEntryEvent extends ParkingEvent {
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final String spotId;
    private final String transactionId;

    public VehicleEntryEvent(String licensePlate, VehicleType vehicleType, String spotId, String transactionId) {
        super("VEHICLE_ENTRY");
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.spotId = spotId;
        this.transactionId = transactionId;
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

    @Override
    public String toString() {
        return "VehicleEntryEvent{" +
                "licensePlate='" + licensePlate + '\'' +
                ", vehicleType=" + vehicleType +
                ", spotId='" + spotId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

