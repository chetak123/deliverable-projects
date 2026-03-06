package com.smartparking.service;

import com.smartparking.event.EventBus;
import com.smartparking.event.SpotAvailabilityChangedEvent;
import com.smartparking.event.VehicleEntryEvent;
import com.smartparking.event.VehicleExitEvent;
import com.smartparking.model.ParkingSpot;
import com.smartparking.model.ParkingTransaction;
import com.smartparking.model.Vehicle;
import com.smartparking.repository.ParkingSpotRepository;
import com.smartparking.repository.TransactionRepository;
import com.smartparking.repository.VehicleRepository;
import com.smartparking.strategy.SpotAllocationStrategy;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Main service for managing parking operations.
 * Handles vehicle entry, exit, and parking spot allocation.
 */
public class ParkingService {
    
    private final VehicleRepository vehicleRepository;
    private final ParkingSpotRepository spotRepository;
    private final TransactionRepository transactionRepository;
    private final FeeCalculationService feeCalculationService;
    private final SpotAllocationStrategy allocationStrategy;
    private final ReentrantLock serviceLock; // For critical operations
    private final EventBus eventBus; // Event-driven architecture
    
    public ParkingService(
            VehicleRepository vehicleRepository,
            ParkingSpotRepository spotRepository,
            TransactionRepository transactionRepository,
            FeeCalculationService feeCalculationService,
            SpotAllocationStrategy allocationStrategy) {
        this.vehicleRepository = vehicleRepository;
        this.spotRepository = spotRepository;
        this.transactionRepository = transactionRepository;
        this.feeCalculationService = feeCalculationService;
        this.allocationStrategy = allocationStrategy;
        this.serviceLock = new ReentrantLock();
        this.eventBus = EventBus.getInstance();
    }
    
    /**
     * Handle vehicle entry (check-in).
     * Thread-safe implementation with pessimistic locking.
     */
    public ParkingTransaction checkIn(Vehicle vehicle) {
        serviceLock.lock();
        try {
            // Check if vehicle already has an active transaction
            Optional<ParkingTransaction> activeTransaction = 
                    transactionRepository.findActiveTransactionByVehicle(vehicle.getLicensePlate());
            
            if (activeTransaction.isPresent()) {
                throw new IllegalStateException(
                    "Vehicle " + vehicle.getLicensePlate() + " already has an active parking session");
            }
            
            // Save vehicle if not exists
            if (!vehicleRepository.exists(vehicle.getLicensePlate())) {
                vehicleRepository.save(vehicle);
            }
            
            // Get available spots
            List<ParkingSpot> availableSpots = spotRepository.findAvailableSpots();
            
            if (availableSpots.isEmpty()) {
                throw new IllegalStateException("No parking spots available");
            }
            
            // Allocate a spot using the strategy
            Optional<ParkingSpot> allocatedSpot = allocationStrategy.allocateSpot(vehicle, availableSpots);
            
            if (!allocatedSpot.isPresent()) {
                throw new IllegalStateException(
                    "No suitable parking spot found for vehicle type: " + vehicle.getVehicleType());
            }
            
            ParkingSpot spot = allocatedSpot.get();
            
            // Occupy the spot
            boolean occupied = spot.occupy(vehicle.getLicensePlate());
            if (!occupied) {
                throw new IllegalStateException("Failed to occupy spot " + spot.getSpotId());
            }
            
            // Create and save transaction
            ParkingTransaction transaction = new ParkingTransaction(
                vehicle.getLicensePlate(),
                spot.getSpotId()
            );
            transactionRepository.save(transaction);

            // Publish events
            eventBus.publish(new VehicleEntryEvent(
                vehicle.getLicensePlate(),
                vehicle.getVehicleType(),
                spot.getSpotId(),
                transaction.getTransactionId()
            ));

            eventBus.publish(new SpotAvailabilityChangedEvent(
                spot.getSpotId(),
                spot.getSpotSize(),
                spot.getFloorNumber(),
                false // Now occupied
            ));

            System.out.println("✓ Vehicle " + vehicle.getLicensePlate() +
                             " checked in at spot " + spot.getSpotId());

            return transaction;
            
        } finally {
            serviceLock.unlock();
        }
    }
    
    /**
     * Handle vehicle exit (check-out).
     * Thread-safe implementation with pessimistic locking.
     */
    public double checkOut(String licensePlate) {
        serviceLock.lock();
        try {
            // Find active transaction
            Optional<ParkingTransaction> transactionOpt = 
                    transactionRepository.findActiveTransactionByVehicle(licensePlate);
            
            if (!transactionOpt.isPresent()) {
                throw new IllegalStateException(
                    "No active parking session found for vehicle: " + licensePlate);
            }
            
            ParkingTransaction transaction = transactionOpt.get();
            
            // Find the vehicle
            Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate)
                    .orElseThrow(() -> new IllegalStateException("Vehicle not found: " + licensePlate));
            
            // Calculate fee
            double fee = feeCalculationService.calculateFee(transaction, vehicle);
            
            // Complete transaction
            transaction.complete(fee);

            // Release the parking spot
            ParkingSpot spot = spotRepository.findById(transaction.getSpotId())
                    .orElseThrow(() -> new IllegalStateException("Spot not found: " + transaction.getSpotId()));

            spot.release();

            // Calculate duration in minutes
            long durationMinutes = Duration.between(
                transaction.getEntryTime(),
                transaction.getExitTime()
            ).toMinutes();

            // Publish events
            eventBus.publish(new VehicleExitEvent(
                vehicle.getLicensePlate(),
                vehicle.getVehicleType(),
                spot.getSpotId(),
                transaction.getTransactionId(),
                fee,
                durationMinutes
            ));

            eventBus.publish(new SpotAvailabilityChangedEvent(
                spot.getSpotId(),
                spot.getSpotSize(),
                spot.getFloorNumber(),
                true // Now available
            ));

            System.out.println("✓ Vehicle " + licensePlate + " checked out from spot " +
                             spot.getSpotId() + ". Fee: $" + String.format("%.2f", fee));

            return fee;

        } finally {
            serviceLock.unlock();
        }
    }

    /**
     * Get current parking status.
     */
    public ParkingStatus getParkingStatus() {
        List<ParkingSpot> allSpots = spotRepository.findAll();
        List<ParkingSpot> availableSpots = spotRepository.findAvailableSpots();

        int totalSpots = allSpots.size();
        int occupiedSpots = totalSpots - availableSpots.size();

        return new ParkingStatus(totalSpots, occupiedSpots, availableSpots.size());
    }

    /**
     * Get transaction details for a vehicle.
     */
    public Optional<ParkingTransaction> getActiveTransaction(String licensePlate) {
        return transactionRepository.findActiveTransactionByVehicle(licensePlate);
    }

    /**
     * Inner class to represent parking status.
     */
    public static class ParkingStatus {
        private final int totalSpots;
        private final int occupiedSpots;
        private final int availableSpots;

        public ParkingStatus(int totalSpots, int occupiedSpots, int availableSpots) {
            this.totalSpots = totalSpots;
            this.occupiedSpots = occupiedSpots;
            this.availableSpots = availableSpots;
        }

        public int getTotalSpots() {
            return totalSpots;
        }

        public int getOccupiedSpots() {
            return occupiedSpots;
        }

        public int getAvailableSpots() {
            return availableSpots;
        }

        @Override
        public String toString() {
            return "ParkingStatus{" +
                    "total=" + totalSpots +
                    ", occupied=" + occupiedSpots +
                    ", available=" + availableSpots +
                    ", occupancy=" + String.format("%.1f%%", (occupiedSpots * 100.0 / totalSpots)) +
                    '}';
        }
    }
}

