# System Architecture - Smart Parking Lot

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Client Application                          │
│                  (ParkingLotApplication)                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Service Layer                              │
│  ┌──────────────────┐              ┌──────────────────┐        │
│  │ ParkingService   │              │ FeeCalculation   │        │
│  │                  │              │ Service          │        │
│  │ - checkIn()      │              │ - calculateFee() │        │
│  │ - checkOut()     │              │ - estimateFee()  │        │
│  │ - getStatus()    │              │                  │        │
│  └──────────────────┘              └──────────────────┘        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Strategy Layer                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         SpotAllocationStrategy (Interface)               │  │
│  └──────────────────────────────────────────────────────────┘  │
│         │                    │                    │             │
│         ▼                    ▼                    ▼             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐        │
│  │FirstAvailable│    │NearestTo    │    │  BestFit    │        │
│  │Strategy      │    │Entrance     │    │  Strategy   │        │
│  └─────────────┘    └─────────────┘    └─────────────┘        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Repository Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │  Vehicle     │  │ ParkingSpot  │  │ Transaction  │         │
│  │  Repository  │  │ Repository   │  │ Repository   │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│         │                  │                  │                 │
│         ▼                  ▼                  ▼                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │  InMemory    │  │  InMemory    │  │  InMemory    │         │
│  │  Vehicle     │  │  ParkingSpot │  │  Transaction │         │
│  │  Repository  │  │  Repository  │  │  Repository  │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Domain Model Layer                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │ Vehicle  │  │ Parking  │  │ Parking  │  │ Parking  │       │
│  │          │  │ Spot     │  │ Trans-   │  │ Floor    │       │
│  │          │  │          │  │ action   │  │          │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
│                                                                 │
│  ┌──────────┐                                                  │
│  │ Parking  │                                                  │
│  │ Lot      │                                                  │
│  └──────────┘                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Component Interaction Flow

### Check-In Flow

```
User → ParkingService.checkIn(vehicle)
         │
         ├─→ VehicleRepository.exists(licensePlate)
         │
         ├─→ TransactionRepository.findActiveTransaction(licensePlate)
         │
         ├─→ ParkingSpotRepository.findAvailableSpots()
         │
         ├─→ SpotAllocationStrategy.allocateSpot(vehicle, spots)
         │
         ├─→ ParkingSpot.occupy(licensePlate)
         │
         └─→ TransactionRepository.save(transaction)
                │
                └─→ Return ParkingTransaction
```

### Check-Out Flow

```
User → ParkingService.checkOut(licensePlate)
         │
         ├─→ TransactionRepository.findActiveTransaction(licensePlate)
         │
         ├─→ VehicleRepository.findByLicensePlate(licensePlate)
         │
         ├─→ FeeCalculationService.calculateFee(transaction, vehicle)
         │
         ├─→ ParkingTransaction.complete(fee)
         │
         └─→ ParkingSpot.release()
                │
                └─→ Return fee amount
```

## Class Relationships

### Domain Model Relationships

```
ParkingLot "1" ──── "N" ParkingFloor
                         │
                         │ "1"
                         │
                         ▼
                    "N" ParkingSpot
                         │
                         │ "0..1"
                         │
                         ▼
                    "1" Vehicle
                         │
                         │ "1"
                         │
                         ▼
                    "N" ParkingTransaction
```

## Concurrency Model

```
┌─────────────────────────────────────────────────────────────┐
│                  Concurrency Layers                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Layer 1: Service-Level Locking                            │
│  ┌───────────────────────────────────────────────────┐     │
│  │  ParkingService (ReentrantLock)                   │     │
│  │  - Prevents concurrent check-in/check-out         │     │
│  └───────────────────────────────────────────────────┘     │
│                         │                                   │
│                         ▼                                   │
│  Layer 2: Entity-Level Synchronization                     │
│  ┌───────────────────────────────────────────────────┐     │
│  │  ParkingSpot (synchronized methods)               │     │
│  │  - occupy(), release()                            │     │
│  │  - Atomic state transitions                       │     │
│  └───────────────────────────────────────────────────┘     │
│                         │                                   │
│                         ▼                                   │
│  Layer 3: Optimistic Locking                               │
│  ┌───────────────────────────────────────────────────┐     │
│  │  Version Fields                                   │     │
│  │  - ParkingSpot.version                            │     │
│  │  - ParkingTransaction.version                     │     │
│  └───────────────────────────────────────────────────┘     │
│                         │                                   │
│                         ▼                                   │
│  Layer 4: Concurrent Data Structures                       │
│  ┌───────────────────────────────────────────────────┐     │
│  │  ConcurrentHashMap in Repositories                │     │
│  │  - Thread-safe storage                            │     │
│  └───────────────────────────────────────────────────┘     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow Diagram

```
┌──────────┐
│  Vehicle │
│  Enters  │
└────┬─────┘
     │
     ▼
┌─────────────────┐
│ License Plate   │
│ Recognition     │
└────┬────────────┘
     │
     ▼
┌─────────────────┐
│ Check Vehicle   │
│ in Repository   │
└────┬────────────┘
     │
     ├─── Exists ───┐
     │              │
     └─ New ────┐   │
                │   │
                ▼   ▼
         ┌──────────────┐
         │ Find Active  │
         │ Transaction  │
         └──────┬───────┘
                │
                ├─── Active ──→ Error
                │
                └─ None ────┐
                            │
                            ▼
                     ┌──────────────┐
                     │ Get Available│
                     │ Spots        │
                     └──────┬───────┘
                            │
                            ▼
                     ┌──────────────┐
                     │ Allocate Spot│
                     │ (Strategy)   │
                     └──────┬───────┘
                            │
                            ▼
                     ┌──────────────┐
                     │ Occupy Spot  │
                     └──────┬───────┘
                            │
                            ▼
                     ┌──────────────┐
                     │ Create       │
                     │ Transaction  │
                     └──────┬───────┘
                            │
                            ▼
                     ┌──────────────┐
                     │ Return Ticket│
                     └──────────────┘
```
