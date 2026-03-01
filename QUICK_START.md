# Quick Start Guide - Smart Parking Lot System

## 🚀 Get Started in 5 Minutes

### Prerequisites
- Java 11+ installed
- Maven 3.6+ installed

### Step 1: Build the Project
```bash
cd deliverable-projects
mvn clean compile
```

### Step 2: Run the Demo
```bash
mvn exec:java -Dexec.mainClass="com.smartparking.ParkingLotApplication"
```

You should see output showing:
- Parking lot initialization
- Multiple vehicle check-ins
- Fee calculations
- Concurrent operations
- Error handling

### Step 3: Understand the Output

The demo runs 5 scenarios:

1. **Multiple Vehicle Check-ins**: Shows how different vehicle types are allocated spots
2. **Vehicle Check-outs**: Demonstrates fee calculation
3. **Concurrent Operations**: Tests thread safety with 5 simultaneous check-ins
4. **Error Handling**: Shows duplicate check-in prevention
5. **Final Status**: Displays parking lot occupancy

## 📝 Code Examples

### Example 1: Basic Check-in/Check-out

```java
// Create repositories
VehicleRepository vehicleRepo = new InMemoryVehicleRepository();
ParkingSpotRepository spotRepo = new InMemoryParkingSpotRepository();
TransactionRepository transactionRepo = new InMemoryTransactionRepository();

// Create services
FeeCalculationService feeService = new FeeCalculationService();
ParkingService parkingService = new ParkingService(
    vehicleRepo, spotRepo, transactionRepo, feeService,
    new FirstAvailableStrategy()
);

// Create a vehicle
Vehicle car = new Vehicle("ABC123", VehicleType.CAR, "John Doe", "555-0001");

// Check in
ParkingTransaction transaction = parkingService.checkIn(car);
System.out.println("Parked at: " + transaction.getSpotId());

// Later... check out
double fee = parkingService.checkOut("ABC123");
System.out.println("Fee: $" + fee);
```

### Example 2: Using Different Allocation Strategies

```java
// Strategy 1: First Available (fastest)
ParkingService fastService = new ParkingService(
    vehicleRepo, spotRepo, transactionRepo, feeService,
    new FirstAvailableStrategy()
);

// Strategy 2: Nearest to Entrance (convenience)
ParkingService convenientService = new ParkingService(
    vehicleRepo, spotRepo, transactionRepo, feeService,
    new NearestToEntranceStrategy()
);

// Strategy 3: Best Fit (space optimization)
ParkingService optimizedService = new ParkingService(
    vehicleRepo, spotRepo, transactionRepo, feeService,
    new BestFitStrategy()
);
```

### Example 3: Creating a Custom Parking Lot

```java
// Create parking lot
ParkingLot parkingLot = new ParkingLot("PL001", "My Parking", "123 Main St");

// Create floor
ParkingFloor floor1 = new ParkingFloor(1);

// Add spots
floor1.addParkingSpot(new ParkingSpot("F1-S1", 1, SpotSize.SMALL, 10));
floor1.addParkingSpot(new ParkingSpot("F1-M1", 1, SpotSize.MEDIUM, 20));
floor1.addParkingSpot(new ParkingSpot("F1-L1", 1, SpotSize.LARGE, 30));

// Add floor to parking lot
parkingLot.addFloor(floor1);

// Load spots into repository
parkingLot.getAllParkingSpots().forEach(spotRepo::save);
```

### Example 4: Checking Parking Status

```java
ParkingService.ParkingStatus status = parkingService.getParkingStatus();

System.out.println("Total Spots: " + status.getTotalSpots());
System.out.println("Occupied: " + status.getOccupiedSpots());
System.out.println("Available: " + status.getAvailableSpots());
```

## 🎯 Key Concepts

### Vehicle Types
- `MOTORCYCLE`: Requires SMALL spot (can fit in any size)
- `CAR`: Requires MEDIUM spot (can fit in MEDIUM, LARGE, EXTRA_LARGE)
- `BUS`: Requires LARGE spot (can fit in LARGE, EXTRA_LARGE)
- `TRUCK`: Requires LARGE spot (can fit in LARGE, EXTRA_LARGE)

### Spot Sizes
- `SMALL`: For motorcycles
- `MEDIUM`: For cars
- `LARGE`: For buses and trucks
- `EXTRA_LARGE`: For oversized vehicles

### Transaction Status
- `ACTIVE`: Vehicle is currently parked
- `COMPLETED`: Vehicle has exited
- `PENDING`: Payment pending
- `CANCELLED`: Transaction cancelled

### Pricing (Default)
- Motorcycle: $2/hour (max $20/day)
- Car: $5/hour (max $50/day)
- Bus: $10/hour (max $100/day)
- Truck: $8/hour (max $80/day)

## 🔧 Customization

### Change Pricing
Edit `FeeCalculationService.java`:
```java
private static final double CAR_HOURLY_RATE = 5.0;
private static final double CAR_MAX_DAILY_RATE = 50.0;
```

### Change Allocation Strategy
When creating `ParkingService`:
```java
new ParkingService(vehicleRepo, spotRepo, transactionRepo, feeService,
    new BestFitStrategy()  // Change this
);
```

## 📚 Next Steps

1. Read [PROJECT_README.md](PROJECT_README.md) for detailed documentation
2. Review [DESIGN_DOCUMENT.md](DESIGN_DOCUMENT.md) for architecture details
3. Explore the source code in `src/main/java/com/smartparking/`
4. Modify the demo in `ParkingLotApplication.java` to test your scenarios

## ❓ Common Issues

### Issue: "No parking spots available"
**Solution**: Increase the number of spots in the parking lot initialization

### Issue: "No suitable parking spot found"
**Solution**: Add more spots of the required size for the vehicle type

### Issue: "Vehicle already has an active parking session"
**Solution**: Check out the vehicle first before checking in again

## 🎓 Learning Path

1. **Beginner**: Run the demo and understand the output
2. **Intermediate**: Modify the demo to add more vehicles and scenarios
3. **Advanced**: Implement a new allocation strategy
4. **Expert**: Add database persistence or REST API

## 📞 Need Help?

- Check the [PROJECT_README.md](PROJECT_README.md) for detailed API documentation
- Review the [DESIGN_DOCUMENT.md](DESIGN_DOCUMENT.md) for architecture insights
- Look at the code comments for inline documentation

---

Happy Coding! 🚗💨
