package com.smartparking.repository;

import com.smartparking.enums.TransactionStatus;
import com.smartparking.model.ParkingTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ParkingTransaction operations.
 */
public interface TransactionRepository {
    
    /**
     * Save a transaction.
     */
    void save(ParkingTransaction transaction);
    
    /**
     * Find a transaction by ID.
     */
    Optional<ParkingTransaction> findById(String transactionId);
    
    /**
     * Find active transaction for a vehicle.
     */
    Optional<ParkingTransaction> findActiveTransactionByVehicle(String licensePlate);
    
    /**
     * Find all transactions for a vehicle.
     */
    List<ParkingTransaction> findByVehicle(String licensePlate);
    
    /**
     * Find transactions by status.
     */
    List<ParkingTransaction> findByStatus(TransactionStatus status);
    
    /**
     * Get all transactions.
     */
    List<ParkingTransaction> findAll();
}

