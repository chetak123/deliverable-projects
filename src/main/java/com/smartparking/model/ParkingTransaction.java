package com.smartparking.model;

import com.smartparking.enums.TransactionStatus;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.UUID;

/**
 * Represents a parking transaction for a vehicle.
 */
public class ParkingTransaction {
    private final String transactionId;
    private final String vehicleLicensePlate;
    private final String spotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private TransactionStatus status;
    private double fee;
    private long version; // For optimistic locking

    public ParkingTransaction(String vehicleLicensePlate, String spotId) {
        this.transactionId = UUID.randomUUID().toString();
        this.vehicleLicensePlate = vehicleLicensePlate;
        this.spotId = spotId;
        this.entryTime = LocalDateTime.now();
        this.exitTime = null;
        this.status = TransactionStatus.ACTIVE;
        this.fee = 0.0;
        this.version = 0L;
    }

    /**
     * Set entry time for testing purposes.
     * This method should only be used in tests.
     */
    public void setEntryTimeForTesting(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getVehicleLicensePlate() {
        return vehicleLicensePlate;
    }

    public String getSpotId() {
        return spotId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public double getFee() {
        return fee;
    }

    public long getVersion() {
        return version;
    }

    /**
     * Complete the transaction with exit time and fee.
     */
    public synchronized void complete(double calculatedFee) {
        if (this.status != TransactionStatus.ACTIVE) {
            throw new IllegalStateException("Transaction is not active");
        }
        this.exitTime = LocalDateTime.now();
        this.fee = calculatedFee;
        this.status = TransactionStatus.COMPLETED;
        this.version++;
    }

    /**
     * Get the duration of parking in minutes.
     */
    public long getDurationInMinutes() {
        LocalDateTime endTime = (exitTime != null) ? exitTime : LocalDateTime.now();
        return Duration.between(entryTime, endTime).toMinutes();
    }

    /**
     * Get the duration of parking in hours (rounded up).
     */
    public long getDurationInHours() {
        long minutes = getDurationInMinutes();
        return (minutes + 59) / 60; // Round up to nearest hour
    }

    @Override
    public String toString() {
        return "ParkingTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", vehicle='" + vehicleLicensePlate + '\'' +
                ", spot='" + spotId + '\'' +
                ", entryTime=" + entryTime +
                ", exitTime=" + exitTime +
                ", status=" + status +
                ", fee=" + fee +
                ", duration=" + getDurationInMinutes() + " mins" +
                '}';
    }
}

