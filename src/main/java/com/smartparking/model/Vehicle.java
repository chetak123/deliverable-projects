package com.smartparking.model;

import com.smartparking.enums.VehicleType;

import java.util.Objects;

/**
 * Represents a vehicle in the parking system.
 */
public class Vehicle {
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final String ownerName;
    private final String ownerPhone;

    public Vehicle(String licensePlate, VehicleType vehicleType, String ownerName, String ownerPhone) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("License plate cannot be null or empty");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null");
        }
        this.licensePlate = licensePlate.trim().toUpperCase();
        this.vehicleType = vehicleType;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return licensePlate.equals(vehicle.licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licensePlate);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "licensePlate='" + licensePlate + '\'' +
                ", vehicleType=" + vehicleType +
                ", ownerName='" + ownerName + '\'' +
                '}';
    }
}

