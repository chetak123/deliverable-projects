# Smart Parking Lot Management System

A comprehensive Java-based backend system for managing smart parking lots with automatic spot allocation, real-time tracking, and intelligent fee calculation.

## 🚗 Features

- **Automatic Spot Allocation**: Intelligently assigns parking spots based on vehicle size and availability
- **Multi-Floor Support**: Manages parking facilities with multiple floors and various spot sizes
- **Real-Time Availability Tracking**: Updates parking spot status in real-time
- **Dynamic Fee Calculation**: Calculates fees based on parking duration and vehicle type
- **Thread-Safe Operations**: Handles concurrent vehicle entries and exits safely
- **Multiple Allocation Strategies**: Supports different algorithms for spot allocation
- **Comprehensive Error Handling**: Robust validation and error management

## 📋 Table of Contents

- [System Requirements](#system-requirements)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Running the Application](#running-the-application)
- [Usage Examples](#usage-examples)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Contributing](#contributing)

## 💻 System Requirements

- Java 11 or higher
- Maven 3.6 or higher
- 4GB RAM minimum
- Operating System: Windows, macOS, or Linux

## 📁 Project Structure

```
smart-parking-system/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── smartparking/
│                   ├── enums/
│                   │   ├── VehicleType.java
│                   │   ├── SpotSize.java
│                   │   ├── TransactionStatus.java
│                   │   └── AllocationStrategy.java
│                   ├── model/
│                   │   ├── Vehicle.java
│                   │   ├── ParkingSpot.java
│                   │   ├── ParkingTransaction.java
│                   │   ├── ParkingFloor.java
│                   │   └── ParkingLot.java
│                   ├── repository/
│                   │   ├── VehicleRepository.java
│                   │   ├── ParkingSpotRepository.java
│                   │   ├── TransactionRepository.java
│                   │   └── impl/
│                   │       ├── InMemoryVehicleRepository.java
│                   │       ├── InMemoryParkingSpotRepository.java
│                   │       └── InMemoryTransactionRepository.java
│                   ├── service/
│                   │   ├── ParkingService.java
│                   │   └── FeeCalculationService.java
│                   ├── strategy/
│                   │   ├── SpotAllocationStrategy.java
│                   │   └── impl/
│                   │       ├── FirstAvailableStrategy.java
│                   │       ├── NearestToEntranceStrategy.java
│                   │       └── BestFitStrategy.java
│                   └── ParkingLotApplication.java
├── pom.xml
├── DESIGN_DOCUMENT.md
└── README.md
```

## 🚀 Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd deliverable-projects
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Verify the build**
   ```bash
   mvn verify
   ```

## ▶️ Running the Application

### Using Maven
```bash
mvn exec:java -Dexec.mainClass="com.smartparking.ParkingLotApplication"
```

### Using Java directly
```bash
# Compile
mvn compile

# Run
java -cp target/classes com.smartparking.ParkingLotApplication
```

## 📖 Usage Examples

### Basic Usage

```java
// Initialize repositories
VehicleRepository vehicleRepo = new InMemoryVehicleRepository();
ParkingSpotRepository spotRepo = new InMemoryParkingSpotRepository();
TransactionRepository transactionRepo = new InMemoryTransactionRepository();

// Initialize services
FeeCalculationService feeService = new FeeCalculationService();
ParkingService parkingService = new ParkingService(
    vehicleRepo,
    spotRepo,
    transactionRepo,
    feeService,
    new FirstAvailableStrategy()
);

// Create a vehicle
Vehicle car = new Vehicle("ABC123", VehicleType.CAR, "John Doe", "555-0001");

// Check in
ParkingTransaction transaction = parkingService.checkIn(car);
System.out.println("Checked in at spot: " + transaction.getSpotId());

// Check out
double fee = parkingService.checkOut("ABC123");
System.out.println("Parking fee: $" + fee);
```

### Using Different Allocation Strategies

```java
// First Available Strategy (fastest)
ParkingService service1 = new ParkingService(
    vehicleRepo, spotRepo, transactionRepo, feeService,
    new FirstAvailableStrategy()
);

// Nearest to Entrance Strategy (convenience)
ParkingService service2 = new ParkingService(
    vehicleRepo, spotRepo, transactionRepo, feeService,
    new NearestToEntranceStrategy()
);

// Best Fit Strategy (space optimization)
ParkingService service3 = new ParkingService(
    vehicleRepo, spotRepo, transactionRepo, feeService,
    new BestFitStrategy()
);
```

### Checking Parking Status

```java
ParkingService.ParkingStatus status = parkingService.getParkingStatus();
System.out.println("Total spots: " + status.getTotalSpots());
System.out.println("Occupied: " + status.getOccupiedSpots());
System.out.println("Available: " + status.getAvailableSpots());
```

## 🏗️ Architecture

The system follows a layered architecture:

1. **Application Layer**: Entry point and demo scenarios
2. **Service Layer**: Business logic and orchestration
3. **Strategy Layer**: Pluggable algorithms for spot allocation
4. **Repository Layer**: Data access abstraction
5. **Domain Model Layer**: Core entities and business objects

See [DESIGN_DOCUMENT.md](DESIGN_DOCUMENT.md) for detailed architecture documentation.

## 🎨 Design Patterns

- **Strategy Pattern**: For spot allocation algorithms
- **Repository Pattern**: For data access abstraction
- **Factory Pattern**: For creating parking lot configurations
- **Singleton Pattern**: For service instances (optional)

## 📚 API Documentation

### ParkingService

#### `checkIn(Vehicle vehicle)`
Handles vehicle entry and spot allocation.

**Parameters**:
- `vehicle`: Vehicle object with license plate, type, and owner details

**Returns**: `ParkingTransaction` object

**Throws**:
- `IllegalStateException`: If vehicle already has an active session
- `IllegalStateException`: If no spots available
- `IllegalStateException`: If no suitable spot for vehicle type

**Example**:
```java
Vehicle car = new Vehicle("XYZ789", VehicleType.CAR, "Jane Smith", "555-0002");
ParkingTransaction transaction = parkingService.checkIn(car);
```

#### `checkOut(String licensePlate)`
Handles vehicle exit and fee calculation.

**Parameters**:
- `licensePlate`: License plate of the vehicle

**Returns**: `double` - Parking fee amount

**Throws**:
- `IllegalStateException`: If no active transaction found

**Example**:
```java
double fee = parkingService.checkOut("XYZ789");
System.out.println("Fee: $" + String.format("%.2f", fee));
```

#### `getParkingStatus()`
Returns current parking lot occupancy status.

**Returns**: `ParkingStatus` object with total, occupied, and available spot counts

**Example**:
```java
ParkingStatus status = parkingService.getParkingStatus();
```

### FeeCalculationService

#### `calculateFee(ParkingTransaction transaction, Vehicle vehicle)`
Calculates parking fee based on duration and vehicle type.

**Pricing**:
- Motorcycle: $2/hour (max $20/day)
- Car: $5/hour (max $50/day)
- Bus: $10/hour (max $100/day)
- Truck: $8/hour (max $80/day)

## 🧪 Testing

### Running Tests
```bash
mvn test
```

### Test Coverage
The system includes tests for:
- Vehicle check-in/check-out operations
- Concurrent operations
- Fee calculation
- Spot allocation strategies
- Error handling

### Manual Testing Scenarios

The application includes built-in demo scenarios:

1. **Multiple Vehicle Check-ins**: Tests basic check-in functionality
2. **Vehicle Check-outs**: Tests fee calculation and spot release
3. **Concurrent Operations**: Tests thread safety with 5 concurrent check-ins
4. **Error Handling**: Tests duplicate check-in prevention
5. **Status Reporting**: Tests real-time availability tracking

## 🔒 Concurrency Handling

The system implements multiple concurrency control mechanisms:

### 1. Pessimistic Locking
- Service-level `ReentrantLock` for critical operations
- Prevents race conditions during check-in/check-out

### 2. Optimistic Locking
- Version field in `ParkingSpot` and `ParkingTransaction`
- Enables conflict detection and retry logic

### 3. Synchronized Methods
- Atomic operations on parking spot state
- Thread-safe state transitions

### 4. Concurrent Data Structures
- `ConcurrentHashMap` in repository implementations
- Thread-safe collections throughout

## 📊 Performance Characteristics

### Time Complexity
- **Check-in**: O(n) where n = number of available spots
- **Check-out**: O(1) with hash-based lookups
- **Get Status**: O(n) where n = total spots

### Space Complexity
- O(V + S + T) where:
  - V = number of vehicles
  - S = number of spots
  - T = number of transactions

## 🔧 Configuration

### Customizing Parking Lot

```java
ParkingLot parkingLot = new ParkingLot("PL001", "My Parking", "123 Main St");

// Add floors
ParkingFloor floor1 = new ParkingFloor(1);

// Add spots
floor1.addParkingSpot(new ParkingSpot("F1-S1", 1, SpotSize.SMALL, 10));
floor1.addParkingSpot(new ParkingSpot("F1-M1", 1, SpotSize.MEDIUM, 20));
floor1.addParkingSpot(new ParkingSpot("F1-L1", 1, SpotSize.LARGE, 30));

parkingLot.addFloor(floor1);
```

### Customizing Fee Rates

Modify the constants in `FeeCalculationService.java`:

```java
private static final double CAR_HOURLY_RATE = 5.0;
private static final double CAR_MAX_DAILY_RATE = 50.0;
```

## 🚀 Future Enhancements

- [ ] REST API implementation
- [ ] Database persistence (PostgreSQL/MySQL)
- [ ] Event-driven architecture with message queues
- [ ] Caching layer (Redis)
- [ ] Payment gateway integration
- [ ] Reservation system
- [ ] Mobile app integration
- [ ] Analytics dashboard
- [ ] Machine learning for predictive allocation
- [ ] License plate recognition integration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is created for educational purposes.

## 👥 Authors

- Smart Parking System Team

## 🙏 Acknowledgments

- Design inspired by real-world parking management systems
- Built following SOLID principles and clean code practices
- Implements patterns from "Design Patterns: Elements of Reusable Object-Oriented Software"

## 📞 Support

For questions or issues, please open an issue in the repository.

---

**Note**: This is a demonstration project showcasing low-level design principles for a smart parking lot system. It uses in-memory storage and is not production-ready without additional enhancements like database persistence, authentication, and monitoring.
