# Smart Parking Lot System - Design Document

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Data Model](#data-model)
4. [Core Components](#core-components)
5. [Algorithms](#algorithms)
6. [Concurrency Handling](#concurrency-handling)
7. [API Design](#api-design)

## System Overview

The Smart Parking Lot System is a comprehensive backend solution for managing multi-floor parking facilities. It handles vehicle entry/exit, automatic spot allocation, real-time availability tracking, and fee calculation.

### Key Features
- **Automatic Spot Allocation**: Intelligently assigns parking spots based on vehicle type and availability
- **Multi-Floor Support**: Manages parking lots with multiple floors and various spot sizes
- **Real-Time Tracking**: Updates parking availability in real-time
- **Fee Calculation**: Calculates fees based on duration and vehicle type
- **Thread-Safe Operations**: Handles concurrent vehicle entries and exits
- **Multiple Allocation Strategies**: Supports different spot allocation algorithms

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│     Application Layer                   │
│  (ParkingLotApplication)                │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│     Service Layer                       │
│  (ParkingService, FeeCalculationService)│
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│     Strategy Layer                      │
│  (SpotAllocationStrategy)               │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│     Repository Layer                    │
│  (VehicleRepo, SpotRepo, TransactionRepo)│
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│     Domain Model Layer                  │
│  (Vehicle, ParkingSpot, Transaction)    │
└─────────────────────────────────────────┘
```

### Design Patterns Used

1. **Strategy Pattern**: For spot allocation algorithms
2. **Repository Pattern**: For data access abstraction
3. **Singleton Pattern**: For service instances
4. **Factory Pattern**: For creating parking lot configurations

## Data Model

### Entity Relationship Diagram

```
Vehicle (1) ──── (N) ParkingTransaction (N) ──── (1) ParkingSpot
   │                                                      │
   │                                                      │
   └──────────────────────────────────────────────────────┘
```

### Core Entities

#### 1. Vehicle
- **Attributes**: licensePlate (PK), vehicleType, ownerName, ownerPhone
- **Purpose**: Represents a vehicle in the system
- **Constraints**: License plate must be unique

#### 2. ParkingSpot
- **Attributes**: spotId (PK), floorNumber, spotSize, distanceFromEntrance, isOccupied, currentVehicleLicensePlate, version
- **Purpose**: Represents a parking spot
- **Constraints**: Spot ID must be unique
- **Concurrency**: Uses version field for optimistic locking

#### 3. ParkingTransaction
- **Attributes**: transactionId (PK), vehicleLicensePlate (FK), spotId (FK), entryTime, exitTime, status, fee, version
- **Purpose**: Tracks parking sessions
- **Constraints**: Only one active transaction per vehicle

#### 4. ParkingFloor
- **Attributes**: floorNumber, parkingSpots[]
- **Purpose**: Groups parking spots by floor
- **Methods**: getAvailableSpots(), getAvailableSpotsBySize()

#### 5. ParkingLot
- **Attributes**: parkingLotId, name, address, floors[]
- **Purpose**: Represents the entire parking facility
- **Methods**: getTotalCapacity(), getTotalAvailableSpots(), isFull()

## Core Components

### 1. ParkingService
Main orchestrator for parking operations.

**Key Methods**:
- `checkIn(Vehicle)`: Handles vehicle entry
- `checkOut(String licensePlate)`: Handles vehicle exit
- `getParkingStatus()`: Returns current occupancy status

**Thread Safety**: Uses ReentrantLock for critical sections

### 2. FeeCalculationService
Calculates parking fees based on duration and vehicle type.

**Pricing Model**:
- Motorcycle: $2/hour (max $20/day)
- Car: $5/hour (max $50/day)
- Bus: $10/hour (max $100/day)
- Truck: $8/hour (max $80/day)

**Features**:
- Hourly rate calculation
- Daily maximum cap
- Time rounding (rounds up to nearest hour)

### 3. SpotAllocationStrategy
Interface for different allocation algorithms.

**Implementations**:
- **FirstAvailableStrategy**: Allocates the first available spot that fits
- **NearestToEntranceStrategy**: Allocates the closest spot to entrance
- **BestFitStrategy**: Allocates the smallest suitable spot (maximizes space utilization)

## Algorithms

### Spot Allocation Algorithm

#### Vehicle-to-Spot Size Mapping
```
MOTORCYCLE → SMALL (can fit in any size)
CAR → MEDIUM (can fit in MEDIUM, LARGE, EXTRA_LARGE)
BUS/TRUCK → LARGE (can fit in LARGE, EXTRA_LARGE)
```

#### First Available Strategy
```
1. Get required spot size for vehicle type
2. Filter available spots that can accommodate the vehicle
3. Return the first matching spot
Time Complexity: O(n) where n = number of available spots
```

#### Nearest to Entrance Strategy
```
1. Get required spot size for vehicle type
2. Filter available spots that can accommodate the vehicle
3. Sort by distance from entrance (ascending)
4. Return the closest spot
Time Complexity: O(n log n) due to sorting
```

#### Best Fit Strategy
```
1. Get required spot size for vehicle type
2. Filter available spots that can accommodate the vehicle
3. Sort by spot size (ascending)
4. Return the smallest suitable spot
Time Complexity: O(n log n) due to sorting
```

### Fee Calculation Algorithm

```
Input: ParkingTransaction, Vehicle
Output: Fee amount

1. Calculate duration in hours (round up)
   duration = ceiling((exitTime - entryTime) / 60 minutes)

2. Get hourly rate for vehicle type
   rate = getHourlyRate(vehicleType)

3. Calculate base fee
   baseFee = duration × rate

4. Apply daily maximum cap
   fee = min(baseFee, maxDailyRate)

5. Return fee
```

## Concurrency Handling

### Thread Safety Mechanisms

#### 1. Pessimistic Locking
Used in `ParkingService` for critical operations:
```java
serviceLock.lock();
try {
    // Critical section: check-in/check-out operations
} finally {
    serviceLock.unlock();
}
```

**Advantages**:
- Prevents race conditions
- Ensures data consistency
- Simple to implement

**Disadvantages**:
- Can create bottlenecks under high load
- Reduced concurrency

#### 2. Optimistic Locking
Implemented in `ParkingSpot` using version field:
```java
public synchronized boolean occupyWithVersionCheck(
    String vehicleLicensePlate,
    long expectedVersion
) {
    if (this.version != expectedVersion || isOccupied) {
        return false; // Conflict detected
    }
    return occupy(vehicleLicensePlate);
}
```

**Advantages**:
- Better performance under low contention
- No lock waiting

**Disadvantages**:
- Requires retry logic
- More complex error handling

#### 3. Synchronized Methods
Used in `ParkingSpot` for atomic operations:
```java
public synchronized boolean occupy(String vehicleLicensePlate) {
    if (isOccupied) return false;
    this.isOccupied = true;
    this.currentVehicleLicensePlate = vehicleLicensePlate;
    this.version++;
    return true;
}
```

#### 4. Concurrent Data Structures
- `ConcurrentHashMap` in repository implementations
- Thread-safe collections for storing entities

### Handling Race Conditions

**Scenario**: Two vehicles trying to occupy the same spot simultaneously

**Solution**:
1. Service-level lock prevents concurrent check-ins
2. Spot-level synchronized methods ensure atomic state changes
3. Version checking provides additional safety layer

## API Design

### ParkingService API

#### Check-In Operation
```java
ParkingTransaction checkIn(Vehicle vehicle)
```
**Flow**:
1. Validate no active transaction exists for vehicle
2. Save vehicle to repository
3. Get available spots
4. Allocate spot using strategy
5. Occupy the spot
6. Create and save transaction
7. Return transaction

**Exceptions**:
- `IllegalStateException`: Vehicle already parked
- `IllegalStateException`: No spots available
- `IllegalStateException`: No suitable spot for vehicle type

#### Check-Out Operation
```java
double checkOut(String licensePlate)
```
**Flow**:
1. Find active transaction for vehicle
2. Calculate parking fee
3. Complete transaction
4. Release parking spot
5. Return fee amount

**Exceptions**:
- `IllegalStateException`: No active transaction found
- `IllegalStateException`: Vehicle not found

#### Get Status
```java
ParkingStatus getParkingStatus()
```
**Returns**: Current occupancy statistics

## Database Schema (Conceptual)

### Tables

#### vehicles
```sql
CREATE TABLE vehicles (
    license_plate VARCHAR(20) PRIMARY KEY,
    vehicle_type VARCHAR(20) NOT NULL,
    owner_name VARCHAR(100),
    owner_phone VARCHAR(20)
);
```

#### parking_spots
```sql
CREATE TABLE parking_spots (
    spot_id VARCHAR(20) PRIMARY KEY,
    floor_number INT NOT NULL,
    spot_size VARCHAR(20) NOT NULL,
    distance_from_entrance INT,
    is_occupied BOOLEAN DEFAULT FALSE,
    current_vehicle_license_plate VARCHAR(20),
    version BIGINT DEFAULT 0,
    FOREIGN KEY (current_vehicle_license_plate) REFERENCES vehicles(license_plate)
);
```

#### parking_transactions
```sql
CREATE TABLE parking_transactions (
    transaction_id VARCHAR(36) PRIMARY KEY,
    vehicle_license_plate VARCHAR(20) NOT NULL,
    spot_id VARCHAR(20) NOT NULL,
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    fee DECIMAL(10,2) DEFAULT 0,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (vehicle_license_plate) REFERENCES vehicles(license_plate),
    FOREIGN KEY (spot_id) REFERENCES parking_spots(spot_id)
);
```

### Indexes
```sql
CREATE INDEX idx_transactions_vehicle ON parking_transactions(vehicle_license_plate);
CREATE INDEX idx_transactions_status ON parking_transactions(status);
CREATE INDEX idx_spots_floor ON parking_spots(floor_number);
CREATE INDEX idx_spots_occupied ON parking_spots(is_occupied);
```

## Scalability Considerations

### Horizontal Scaling
- Stateless service design allows multiple instances
- Repository pattern enables easy database sharding
- Event-driven architecture for real-time updates

### Performance Optimization
- Indexing on frequently queried fields
- Caching of parking lot configuration
- Connection pooling for database access
- Asynchronous processing for non-critical operations

### Future Enhancements
1. **Event-Driven Architecture**: Publish events for entry/exit
2. **Caching Layer**: Redis for real-time availability
3. **Message Queue**: For handling peak loads
4. **Microservices**: Split into separate services (Entry, Exit, Billing)
5. **Machine Learning**: Predictive allocation based on patterns
6. **Mobile Integration**: REST API for mobile apps
7. **Payment Gateway**: Integration with payment systems
8. **Reservation System**: Pre-booking of parking spots

