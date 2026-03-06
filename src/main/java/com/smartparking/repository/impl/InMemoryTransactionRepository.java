package com.smartparking.repository.impl;

import com.smartparking.enums.TransactionStatus;
import com.smartparking.model.ParkingTransaction;
import com.smartparking.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of TransactionRepository.
 * Thread-safe using ConcurrentHashMap.
 */
public class InMemoryTransactionRepository implements TransactionRepository {
    
    private final Map<String, ParkingTransaction> transactions = new ConcurrentHashMap<>();
    
    @Override
    public void save(ParkingTransaction transaction) {
        transactions.put(transaction.getTransactionId(), transaction);
    }
    
    @Override
    public Optional<ParkingTransaction> findById(String transactionId) {
        return Optional.ofNullable(transactions.get(transactionId));
    }
    
    @Override
    public Optional<ParkingTransaction> findActiveTransactionByVehicle(String licensePlate) {
        return transactions.values().stream()
                .filter(t -> t.getVehicleLicensePlate().equalsIgnoreCase(licensePlate))
                .filter(t -> t.getStatus() == TransactionStatus.ACTIVE)
                .findFirst();
    }
    
    @Override
    public List<ParkingTransaction> findByVehicle(String licensePlate) {
        return transactions.values().stream()
                .filter(t -> t.getVehicleLicensePlate().equalsIgnoreCase(licensePlate))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ParkingTransaction> findByStatus(TransactionStatus status) {
        return transactions.values().stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ParkingTransaction> findAll() {
        return new ArrayList<>(transactions.values());
    }
}

