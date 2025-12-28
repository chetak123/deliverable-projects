
# LearnTrack Architecture Diagram

This diagram shows the layered architecture of the LearnTrack application.

```mermaid
graph TB
    subgraph "UI Layer"
        Main[Main.java<br/>Console Interface]
    end
    
    subgraph "Service Layer - Business Logic"
        SS[StudentService]
        CS[CourseService]
        ES[EnrollmentService]
    end
    
    subgraph "Repository Layer - Data Storage"
        SR[StudentRepository<br/>ArrayList&lt;Student&gt;]
        CR[CourseRepository<br/>ArrayList&lt;Course&gt;]
        ER[EnrollmentRepository<br/>ArrayList&lt;Enrollment&gt;]
    end
    
    subgraph "Entity Layer - Domain Models"
        Person[Person<br/>Base Class]
        Student[Student<br/>extends Person]
        Course[Course]
        Enrollment[Enrollment]
    end
    
    subgraph "Support Components"
        Util[Utilities<br/>IdGenerator<br/>InputValidator]
        Const[Constants<br/>MenuOptions<br/>AppConstants]
        Enum[Enums<br/>EnrollmentStatus<br/>CourseStatus]
        Exc[Exceptions<br/>EntityNotFoundException<br/>InvalidInputException]
    end
    
    Main --> SS
    Main --> CS
    Main --> ES
    
    SS --> SR
    CS --> CR
    ES --> ER
    ES --> SS
    ES --> CS
    
    SR --> Student
    CR --> Course
    ER --> Enrollment
    
    Student -.inherits.-> Person
    
    SS --> Util
    CS --> Util
    ES --> Util
    
    Main --> Const
    Course --> Enum
    Enrollment --> Enum
    
    SS --> Exc
    CS --> Exc
    ES --> Exc
    
    style Main fill:#e1f5ff
    style SS fill:#fff4e1
    style CS fill:#fff4e1
    style ES fill:#fff4e1
    style SR fill:#e8f5e9
    style CR fill:#e8f5e9
    style ER fill:#e8f5e9
    style Person fill:#f3e5f5
    style Student fill:#f3e5f5
    style Course fill:#f3e5f5
    style Enrollment fill:#f3e5f5
```

## Layer Descriptions

### UI Layer
- **Main.java**: Entry point and console interface for user interaction
- Handles menu display and user input
- Delegates business logic to service layer

### Service Layer
- **StudentService**: Business logic for student management
- **CourseService**: Business logic for course management
- **EnrollmentService**: Business logic for enrollment management
- Validates input and coordinates between repositories

### Repository Layer
- **StudentRepository**: In-memory storage for students using ArrayList
- **CourseRepository**: In-memory storage for courses using ArrayList
- **EnrollmentRepository**: In-memory storage for enrollments using ArrayList
- Handles CRUD operations

### Entity Layer
- **Person**: Base class with common attributes
- **Student**: Extends Person, represents a student
- **Course**: Represents a course
- **Enrollment**: Links students to courses

### Support Components
- **Utilities**: Helper classes (IdGenerator, InputValidator)
- **Constants**: Application constants (MenuOptions, AppConstants)
- **Enums**: Status enumerations (EnrollmentStatus, CourseStatus)
- **Exceptions**: Custom exceptions for error handling

