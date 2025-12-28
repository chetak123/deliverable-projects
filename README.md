# LearnTrack - Student & Course Management System

[![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A console-based Student & Course Management System built with Core Java, demonstrating fundamental OOP principles, clean code practices, and proper software architecture.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [Learning Objectives](#learning-objectives)
- [Documentation](#documentation)
- [Technologies Used](#technologies-used)
- [Contributing](#contributing)

## 🎯 Overview

LearnTrack is an educational project designed to practice and demonstrate Core Java fundamentals including:
- Object-Oriented Programming (OOP) principles
- Collections (ArrayList)
- Exception handling
- Clean code and modular design
- Layered architecture

The system allows administrators to manage students, courses, and enrollments through an interactive console interface.

## ✨ Features

### Student Management
- ➕ Add new students with validation
- 📋 View all students
- 🔍 Search students by ID
- ✏️ Update student information
- 🚫 Deactivate/Activate students (soft delete)

### Course Management
- ➕ Add new courses with duration
- 📋 View all courses
- 🔍 Search courses by ID
- ✏️ Update course details
- 🔄 Toggle course status (Active/Inactive)

### Enrollment Management
- 📝 Enroll students in courses
- 👤 View enrollments by student
- 📚 View enrollments by course
- 🔄 Update enrollment status (Active/Completed/Cancelled)
- 📊 View all enrollments

## 📁 Project Structure

```
deliverable-projects/
├── src/
│   └── com/
│       └── airtribe/
│           └── learntrack/
│               ├── Main.java                    # Application entry point
│               ├── entity/                      # Domain models
│               │   ├── Person.java             # Base class
│               │   ├── Student.java            # Student entity
│               │   ├── Course.java             # Course entity
│               │   └── Enrollment.java         # Enrollment entity
│               ├── repository/                  # Data storage layer
│               │   ├── StudentRepository.java
│               │   ├── CourseRepository.java
│               │   └── EnrollmentRepository.java
│               ├── service/                     # Business logic layer
│               │   ├── StudentService.java
│               │   ├── CourseService.java
│               │   └── EnrollmentService.java
│               ├── exception/                   # Custom exceptions
│               │   ├── EntityNotFoundException.java
│               │   └── InvalidInputException.java
│               ├── util/                        # Utility classes
│               │   ├── IdGenerator.java        # Static ID generation
│               │   └── InputValidator.java     # Input validation
│               ├── constants/                   # Application constants
│               │   ├── MenuOptions.java
│               │   └── AppConstants.java
│               └── enums/                       # Enumerations
│                   ├── EnrollmentStatus.java
│                   └── CourseStatus.java
├── docs/                                        # Documentation
│   ├── Setup_Instructions.md                   # Installation guide
│   ├── JVM_Basics.md                           # JVM concepts explained
│   └── Design_Notes.md                         # Architecture & design decisions
└── README.md                                    # This file
```

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK) 11 or higher**
- A text editor or IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/deliverable-projects.git
   cd deliverable-projects
   ```

2. **Compile the project:**
   ```bash
   javac -d out src/com/airtribe/learntrack/**/*.java src/com/airtribe/learntrack/*.java
   ```

3. **Run the application:**
   ```bash
   java -cp out com.airtribe.learntrack.Main
   ```

For detailed setup instructions, see [Setup_Instructions.md](docs/Setup_Instructions.md).

## 💻 Usage

### Main Menu

When you run the application, you'll see:

```
===========================================
   Welcome to LearnTrack
   Student & Course Management System
   Version: 1.0.0
===========================================

========== Main Menu ==========
1. Student Management
2. Course Management
3. Enrollment Management
0. Exit
===============================
```

### Example Workflow

1. **Add a Student:**
   - Select `1` (Student Management)
   - Select `1` (Add New Student)
   - Enter student details (name, email, batch)

2. **Add a Course:**
   - Select `2` (Course Management)
   - Select `1` (Add New Course)
   - Enter course details (name, description, duration)

3. **Enroll Student:**
   - Select `3` (Enrollment Management)
   - Select `1` (Enroll Student in Course)
   - Enter student ID and course ID

## 🎓 Learning Objectives

This project demonstrates:

### Core Java Concepts
- ✅ Variables, data types, and control flow
- ✅ Classes, objects, and constructors
- ✅ Constructor overloading
- ✅ Static vs instance members
- ✅ Packages and access modifiers

### OOP Principles
- ✅ **Encapsulation**: Private fields with public getters/setters
- ✅ **Inheritance**: Student extends Person
- ✅ **Polymorphism**: Method overriding (getDisplayName)
- ✅ **Abstraction**: Layered architecture

### Collections
- ✅ ArrayList for dynamic data storage
- ✅ Understanding ArrayList vs Array
- ✅ CRUD operations on collections

### Exception Handling
- ✅ Custom exceptions (EntityNotFoundException, InvalidInputException)
- ✅ Try-catch blocks for graceful error handling
- ✅ Input validation

### Clean Code Practices
- ✅ Separation of concerns (Entity, Repository, Service, UI)
- ✅ Meaningful naming conventions
- ✅ Small, focused methods
- ✅ Proper code organization

## 📚 Documentation

- **[Setup Instructions](docs/Setup_Instructions.md)**: How to install Java and run the project
- **[JVM Basics](docs/JVM_Basics.md)**: Understanding JDK, JRE, JVM, and bytecode
- **[Design Notes](docs/Design_Notes.md)**: Architecture decisions and design patterns

### 📊 Visual Diagrams

- **[Architecture Diagram](docs/diagrams/Architecture.md)**: Layered architecture visualization
- **[OOP Concepts Diagram](docs/diagrams/OOP_Concepts.md)**: OOP principles illustrated
- **[Class Diagram](docs/diagrams/Class_Diagram.md)**: Complete UML class diagram
- **[All Diagrams](docs/diagrams/)**: Browse all visual documentation

## 🛠️ Technologies Used

- **Language**: Java 11+
- **Collections**: ArrayList
- **Architecture**: Layered (Entity, Repository, Service, UI)
- **Design Patterns**: Repository Pattern, Service Layer Pattern

## 🤝 Contributing

This is an educational project. Contributions, suggestions, and feedback are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/improvement`)
3. Commit your changes (`git commit -m 'Add some improvement'`)
4. Push to the branch (`git push origin feature/improvement`)
5. Open a Pull Request

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 👨‍💻 Author

Created as part of the Airtribe Java learning curriculum.

## 🙏 Acknowledgments

- Airtribe for the project requirements and learning objectives
- The Java community for excellent documentation and resources

---

**Happy Learning! 🚀**
