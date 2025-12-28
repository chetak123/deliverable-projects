# LearnTrack Class Diagram

This diagram shows the class relationships and structure in the LearnTrack application.

```mermaid
classDiagram
    %% Entity Classes
    class Person {
        -int id
        -String firstName
        -String lastName
        -String email
        +Person()
        +Person(id, firstName, lastName, email)
        +getId() int
        +setId(int)
        +getFirstName() String
        +setFirstName(String)
        +getLastName() String
        +setLastName(String)
        +getEmail() String
        +setEmail(String)
        +getDisplayName() String
    }
    
    class Student {
        -String batch
        -boolean active
        +Student()
        +Student(id, firstName, lastName, email, batch)
        +getBatch() String
        +setBatch(String)
        +isActive() boolean
        +setActive(boolean)
        +getDisplayName() String
    }
    
    class Course {
        -int id
        -String courseName
        -String description
        -int durationInWeeks
        -CourseStatus status
        +Course()
        +Course(id, courseName, description, duration)
        +getId() int
        +getCourseName() String
        +getStatus() CourseStatus
        +toggleStatus() void
    }
    
    class Enrollment {
        -int id
        -int studentId
        -int courseId
        -LocalDate enrollmentDate
        -EnrollmentStatus status
        +Enrollment()
        +Enrollment(id, studentId, courseId)
        +getId() int
        +getStudentId() int
        +getCourseId() int
        +getStatus() EnrollmentStatus
        +setStatus(EnrollmentStatus)
    }
    
    %% Enums
    class EnrollmentStatus {
        <<enumeration>>
        ACTIVE
        COMPLETED
        CANCELLED
    }
    
    class CourseStatus {
        <<enumeration>>
        ACTIVE
        INACTIVE
    }
    
    %% Repository Classes
    class StudentRepository {
        -ArrayList~Student~ students
        +save(Student) void
        +findById(int) Optional~Student~
        +findAll() ArrayList~Student~
        +update(Student) boolean
        +deleteById(int) boolean
    }
    
    class CourseRepository {
        -ArrayList~Course~ courses
        +save(Course) void
        +findById(int) Optional~Course~
        +findAll() ArrayList~Course~
        +update(Course) boolean
    }
    
    class EnrollmentRepository {
        -ArrayList~Enrollment~ enrollments
        +save(Enrollment) void
        +findById(int) Optional~Enrollment~
        +findByStudentId(int) ArrayList~Enrollment~
        +findByCourseId(int) ArrayList~Enrollment~
    }
    
    %% Service Classes
    class StudentService {
        -StudentRepository repository
        +addStudent(firstName, lastName, email, batch) Student
        +findStudentById(int) Student
        +getAllStudents() ArrayList~Student~
        +updateStudent(id, firstName, lastName, email, batch) void
        +deactivateStudent(int) void
    }
    
    class CourseService {
        -CourseRepository repository
        +addCourse(name, description, duration) Course
        +findCourseById(int) Course
        +getAllCourses() ArrayList~Course~
        +updateCourse(id, name, description, duration) void
        +toggleCourseStatus(int) void
    }
    
    class EnrollmentService {
        -EnrollmentRepository repository
        -StudentService studentService
        -CourseService courseService
        +enrollStudent(studentId, courseId) Enrollment
        +getEnrollmentsByStudent(int) ArrayList~Enrollment~
        +getEnrollmentsByCourse(int) ArrayList~Enrollment~
        +updateEnrollmentStatus(id, status) void
    }
    
    %% Utility Classes
    class IdGenerator {
        <<utility>>
        -static int studentIdCounter
        -static int courseIdCounter
        -static int enrollmentIdCounter
        +static getNextStudentId() int
        +static getNextCourseId() int
        +static getNextEnrollmentId() int
    }
    
    class InputValidator {
        <<utility>>
        +static isValidName(String) boolean
        +static isValidEmail(String) boolean
        +static isValidDuration(int) boolean
        +static isValidId(int) boolean
    }
    
    %% Exception Classes
    class EntityNotFoundException {
        <<exception>>
        +EntityNotFoundException(message)
    }
    
    class InvalidInputException {
        <<exception>>
        +InvalidInputException(message)
    }
    
    %% Relationships
    Person <|-- Student : extends
    
    Course --> CourseStatus : uses
    Enrollment --> EnrollmentStatus : uses
    
    StudentRepository --> Student : stores
    CourseRepository --> Course : stores
    EnrollmentRepository --> Enrollment : stores
    
    StudentService --> StudentRepository : uses
    StudentService --> InputValidator : uses
    StudentService --> IdGenerator : uses
    
    CourseService --> CourseRepository : uses
    CourseService --> InputValidator : uses
    CourseService --> IdGenerator : uses
    
    EnrollmentService --> EnrollmentRepository : uses
    EnrollmentService --> StudentService : uses
    EnrollmentService --> CourseService : uses
    EnrollmentService --> IdGenerator : uses
    
    StudentService ..> EntityNotFoundException : throws
    StudentService ..> InvalidInputException : throws
    CourseService ..> EntityNotFoundException : throws
    CourseService ..> InvalidInputException : throws
    EnrollmentService ..> EntityNotFoundException : throws
    EnrollmentService ..> InvalidInputException : throws
```

## Class Descriptions

### Entity Layer

#### Person (Base Class)
- Abstract representation of a person
- Contains common attributes: id, firstName, lastName, email
- Provides base implementation of `getDisplayName()`

#### Student (Extends Person)
- Represents a student in the system
- Adds student-specific fields: batch, active status
- Overrides `getDisplayName()` to include batch information

#### Course
- Represents a course offering
- Contains course details: name, description, duration
- Has a status (ACTIVE/INACTIVE)

#### Enrollment
- Links a student to a course
- Tracks enrollment date and status
- Represents the many-to-many relationship between students and courses

### Repository Layer

#### StudentRepository
- Manages in-memory storage of students using ArrayList
- Provides CRUD operations for Student entities
- Returns Optional for safe null handling

#### CourseRepository
- Manages in-memory storage of courses using ArrayList
- Provides CRUD operations for Course entities

#### EnrollmentRepository
- Manages in-memory storage of enrollments using ArrayList
- Provides specialized queries (by student, by course)
- Checks for duplicate enrollments

### Service Layer

#### StudentService
- Business logic for student management
- Validates input before creating/updating students
- Uses StudentRepository for data access
- Throws custom exceptions for error cases

#### CourseService
- Business logic for course management
- Validates course data
- Manages course status transitions

#### EnrollmentService
- Business logic for enrollment management
- Coordinates between StudentService and CourseService
- Validates that students and courses exist before enrollment
- Prevents duplicate enrollments

### Utility Classes

#### IdGenerator
- Static utility for generating unique IDs
- Maintains separate counters for students, courses, and enrollments
- Thread-safe for single-threaded applications

#### InputValidator
- Static utility for input validation
- Validates names, emails, durations, and IDs
- Centralizes validation logic

### Exception Classes

#### EntityNotFoundException
- Thrown when a requested entity (Student, Course, Enrollment) is not found
- Extends Exception (checked exception)

#### InvalidInputException
- Thrown when user input fails validation
- Extends Exception (checked exception)

## Key Design Patterns

1. **Repository Pattern**: Separates data access from business logic
2. **Service Layer Pattern**: Encapsulates business logic
3. **Inheritance**: Student extends Person
4. **Encapsulation**: Private fields with public accessors
5. **Static Utility Pattern**: IdGenerator and InputValidator

