# Design Notes

## Project Architecture

LearnTrack follows a **layered architecture** pattern with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         UI Layer (Main.java)            │  ← User interaction, menus
├─────────────────────────────────────────┤
│      Service Layer (Business Logic)     │  ← StudentService, CourseService, EnrollmentService
├─────────────────────────────────────────┤
│    Repository Layer (Data Storage)      │  ← StudentRepository, CourseRepository, EnrollmentRepository
├─────────────────────────────────────────┤
│      Entity Layer (Domain Models)       │  ← Student, Course, Enrollment, Person
└─────────────────────────────────────────┘
```

---

## Why ArrayList Instead of Array?

### Arrays in Java:
- **Fixed size**: Once created, size cannot change
- **Manual management**: Need to track how many elements are actually used
- **No built-in methods**: Must write custom code for searching, adding, removing

### ArrayList Advantages:
- **Dynamic size**: Automatically grows/shrinks as needed
- **Rich API**: Built-in methods like `add()`, `remove()`, `contains()`, `size()`
- **Type safety**: Generics ensure type correctness (e.g., `ArrayList<Student>`)
- **Easier to use**: Less boilerplate code

### Example Comparison:

**Using Array:**
```java
Student[] students = new Student[10];  // Fixed size
int count = 0;

// Adding a student
if (count < students.length) {
    students[count++] = newStudent;
}

// What if we need more than 10? Need to create new array and copy!
```

**Using ArrayList:**
```java
ArrayList<Student> students = new ArrayList<>();

// Adding a student
students.add(newStudent);  // Automatically handles resizing!
```

### When We Use ArrayList in LearnTrack:
- **StudentRepository**: Stores all students
- **CourseRepository**: Stores all courses
- **EnrollmentRepository**: Stores all enrollments

This allows the system to handle any number of students, courses, and enrollments without worrying about size limits.

---

## Where We Used Static Members and Why

### 1. IdGenerator Utility Class

**Static Fields:**
```java
private static int studentIdCounter = 1000;
private static int courseIdCounter = 2000;
private static int enrollmentIdCounter = 3000;
```

**Why Static?**
- We need **one shared counter** across the entire application
- Every time we create a student, we need the next ID from the same sequence
- If these were instance variables, each `IdGenerator` object would have its own counter (wrong!)

**Static Methods:**
```java
public static int getNextStudentId() {
    return ++studentIdCounter;
}
```

**Why Static?**
- We don't need to create an `IdGenerator` object to get an ID
- Can call directly: `IdGenerator.getNextStudentId()`
- Makes sense because ID generation is a utility function, not tied to any specific object

### 2. Constants Classes

**MenuOptions and AppConstants:**
```java
public class MenuOptions {
    public static final int MAIN_STUDENT_MANAGEMENT = 1;
    public static final int MAIN_COURSE_MANAGEMENT = 2;
    // ...
}
```

**Why Static?**
- Constants should be shared across the entire application
- No need to create instances of these classes
- Can access directly: `MenuOptions.MAIN_STUDENT_MANAGEMENT`

### 3. InputValidator Utility Class

**Static Methods:**
```java
public static boolean isValidEmail(String email) {
    // validation logic
}
```

**Why Static?**
- Validation is a stateless operation (doesn't depend on object state)
- Makes sense as a utility function
- Can call directly: `InputValidator.isValidEmail(email)`

### Static vs Instance: Decision Guide

| Use Static When | Use Instance When |
|----------------|-------------------|
| Shared across all instances | Unique to each object |
| Utility/helper functions | Object behavior |
| Constants | Object state |
| No object state needed | Needs object state |

---

## Where We Used Inheritance and What We Gained

### Inheritance Hierarchy:

```
        Person (Base Class)
           ↓
        Student (Derived Class)
```

### Person Class (Base):
```java
public class Person {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    
    public String getDisplayName() {
        return firstName + " " + lastName;
    }
}
```

### Student Class (Derived):
```java
public class Student extends Person {
    private String batch;
    private boolean active;
    
    @Override
    public String getDisplayName() {
        return super.getDisplayName() + " (Batch: " + batch + ")";
    }
}
```

### What We Gained:

#### 1. **Code Reuse**
- Student automatically gets `id`, `firstName`, `lastName`, `email` fields
- No need to duplicate these fields in Student class
- Getters/setters inherited from Person

#### 2. **Polymorphism**
- Can override `getDisplayName()` to add batch information
- Demonstrates method overriding
- Student has specialized behavior while maintaining Person's contract

#### 3. **Extensibility**
- Easy to add more person types (e.g., `Trainer extends Person`)
- Common person-related logic stays in one place
- Changes to Person automatically apply to all subclasses

#### 4. **Logical Hierarchy**
- Models real-world relationship: "A Student IS-A Person"
- Makes code more intuitive and maintainable

### Constructor Chaining with `super`:

```java
public Student(int id, String firstName, String lastName, String email, String batch) {
    super(id, firstName, lastName, email);  // Call Person's constructor
    this.batch = batch;
    this.active = true;
}
```

**Benefits:**
- Reuses Person's initialization logic
- Ensures proper initialization of inherited fields
- Maintains encapsulation

---

## Design Patterns Used

### 1. **Repository Pattern**
- Separates data access logic from business logic
- Repositories handle all CRUD operations
- Easy to swap in-memory storage with database later

### 2. **Service Layer Pattern**
- Business logic separated from UI and data access
- Services coordinate between repositories and UI
- Makes testing easier

### 3. **Dependency Injection (Simple)**
```java
private static final StudentService studentService = new StudentService();
private static final CourseService courseService = new CourseService();
private static final EnrollmentService enrollmentService = 
    new EnrollmentService(studentService, courseService);
```
- EnrollmentService depends on StudentService and CourseService
- Dependencies injected through constructor
- Loose coupling between components

---

## Exception Handling Strategy

### Custom Exceptions:
1. **EntityNotFoundException**: When a student/course/enrollment is not found
2. **InvalidInputException**: When user input fails validation

### Try-Catch Blocks:
- **Input parsing**: Catches `NumberFormatException` when user enters non-numeric input
- **Menu operations**: Catches custom exceptions and displays user-friendly messages
- **Graceful degradation**: Application never crashes, always shows helpful error messages

### Example:
```java
try {
    Student student = studentService.findStudentById(id);
    // ... process student
} catch (EntityNotFoundException e) {
    System.out.println("Error: " + e.getMessage());
} catch (Exception e) {
    System.out.println("An unexpected error occurred: " + e.getMessage());
}
```

---

## Key OOP Principles Demonstrated

### 1. **Encapsulation**
- All entity fields are `private`
- Access through public getters/setters
- Internal implementation hidden from outside

### 2. **Inheritance**
- Student extends Person
- Code reuse and logical hierarchy

### 3. **Polymorphism**
- Method overriding (`getDisplayName()`)
- Different behavior for Student vs Person

### 4. **Abstraction**
- Service layer abstracts business logic
- Repository layer abstracts data storage
- UI doesn't know how data is stored

---

## Future Enhancements

Possible improvements for learning more advanced concepts:
1. **File Persistence**: Save data to files instead of in-memory
2. **Database Integration**: Use JDBC to connect to a database
3. **Streams API**: Use Java 8+ streams for filtering and processing collections
4. **Multi-threading**: Handle concurrent operations
5. **Design Patterns**: Implement Singleton, Factory, Observer patterns
6. **Unit Testing**: Add JUnit tests for all services
7. **Logging**: Add proper logging instead of System.out.println

