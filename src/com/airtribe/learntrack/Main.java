package com.airtribe.learntrack;

import com.airtribe.learntrack.constants.AppConstants;
import com.airtribe.learntrack.constants.MenuOptions;
import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.entity.Enrollment;
import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.enums.EnrollmentStatus;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;
import com.airtribe.learntrack.service.CourseService;
import com.airtribe.learntrack.service.EnrollmentService;
import com.airtribe.learntrack.service.StudentService;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main class - Entry point for LearnTrack application
 * Demonstrates menu-driven console application with proper exception handling
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    // Service instances (Dependency Injection pattern - simple version)
    private static final StudentService studentService = new StudentService();
    private static final CourseService courseService = new CourseService();
    private static final EnrollmentService enrollmentService = new EnrollmentService(studentService, courseService);

    public static void main(String[] args) {
        printWelcomeBanner();

        boolean exit = false;

        while (!exit) {
            printMainMenu();
            int choice = getIntInput("Enter your choice: ");

            try {
                switch (choice) {
                    case MenuOptions.MAIN_STUDENT_MANAGEMENT:
                        handleStudentManagement();
                        break;
                    case MenuOptions.MAIN_COURSE_MANAGEMENT:
                        handleCourseManagement();
                        break;
                    case MenuOptions.MAIN_ENROLLMENT_MANAGEMENT:
                        handleEnrollmentManagement();
                        break;
                    case MenuOptions.MAIN_EXIT:
                        exit = true;
                        System.out.println("\nExiting " + AppConstants.APP_NAME + ". Goodbye!");
                        break;
                    default:
                        System.out.println(AppConstants.INVALID_INPUT_MSG);
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
        scanner.close();
    }

    // ==================== WELCOME BANNER ====================

    private static void printWelcomeBanner() {
        System.out.println("===========================================");
        System.out.println("   Welcome to " + AppConstants.APP_NAME);
        System.out.println("   " + AppConstants.APP_DESCRIPTION);
        System.out.println("   Version: " + AppConstants.APP_VERSION);
        System.out.println("===========================================");
    }

    // ==================== MAIN MENU ====================

    private static void printMainMenu() {
        System.out.println("\n========== Main Menu ==========");
        System.out.println("1. Student Management");
        System.out.println("2. Course Management");
        System.out.println("3. Enrollment Management");
        System.out.println("0. Exit");
        System.out.println("===============================");
    }

    // ==================== STUDENT MANAGEMENT ====================

    private static void handleStudentManagement() {
        boolean back = false;

        while (!back) {
            printStudentMenu();
            int choice = getIntInput("Enter your choice: ");

            try {
                switch (choice) {
                    case MenuOptions.STUDENT_ADD:
                        addStudent();
                        break;
                    case MenuOptions.STUDENT_VIEW_ALL:
                        viewAllStudents();
                        break;
                    case MenuOptions.STUDENT_SEARCH:
                        searchStudent();
                        break;
                    case MenuOptions.STUDENT_UPDATE:
                        updateStudent();
                        break;
                    case MenuOptions.STUDENT_DEACTIVATE:
                        deactivateStudent();
                        break;
                    case MenuOptions.STUDENT_BACK:
                        back = true;
                        break;
                    default:
                        System.out.println(AppConstants.INVALID_INPUT_MSG);
                }
            } catch (EntityNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (InvalidInputException e) {
                System.out.println("Validation Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private static void printStudentMenu() {
        System.out.println("\n----- Student Management -----");
        System.out.println("1. Add New Student");
        System.out.println("2. View All Students");
        System.out.println("3. Search Student by ID");
        System.out.println("4. Update Student");
        System.out.println("5. Deactivate Student");
        System.out.println("0. Back to Main Menu");
        System.out.println("------------------------------");
    }

    private static void addStudent() throws InvalidInputException {
        System.out.println("\n--- Add New Student ---");

        String firstName = getStringInput("Enter First Name: ");
        String lastName = getStringInput("Enter Last Name: ");
        String email = getStringInput("Enter Email: ");
        String batch = getStringInput("Enter Batch: ");

        Student student = studentService.addStudent(firstName, lastName, email, batch);
        System.out.println("\n✓ Student added successfully!");
        System.out.println("Student ID: " + student.getId());
        System.out.println(student);
    }

    private static void viewAllStudents() {
        System.out.println("\n--- All Students ---");
        ArrayList<Student> students = studentService.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("No students found.");
        } else {
            System.out.println("Total Students: " + students.size());
            System.out.println("---------------------------------------------------");
            for (Student student : students) {
                System.out.println(student);
            }
        }
    }

    private static void searchStudent() throws EntityNotFoundException {
        System.out.println("\n--- Search Student ---");
        int id = getIntInput("Enter Student ID: ");

        Student student = studentService.findStudentById(id);
        System.out.println("\nStudent Found:");
        System.out.println(student);
        System.out.println("Display Name: " + student.getDisplayName());
    }

    private static void updateStudent() throws EntityNotFoundException, InvalidInputException {
        System.out.println("\n--- Update Student ---");
        int id = getIntInput("Enter Student ID: ");

        // First, show current details
        Student student = studentService.findStudentById(id);
        System.out.println("\nCurrent Details:");
        System.out.println(student);

        System.out.println("\nEnter New Details:");
        String firstName = getStringInput("Enter First Name: ");
        String lastName = getStringInput("Enter Last Name: ");
        String email = getStringInput("Enter Email: ");
        String batch = getStringInput("Enter Batch: ");

        studentService.updateStudent(id, firstName, lastName, email, batch);
        System.out.println("\n✓ Student updated successfully!");
    }

    private static void deactivateStudent() throws EntityNotFoundException {
        System.out.println("\n--- Deactivate Student ---");
        int id = getIntInput("Enter Student ID: ");

        Student student = studentService.findStudentById(id);
        System.out.println("\nStudent to deactivate:");
        System.out.println(student);

        String confirm = getStringInput("Are you sure? (yes/no): ");
        if (confirm.equalsIgnoreCase("yes")) {
            studentService.deactivateStudent(id);
            System.out.println("\n✓ Student deactivated successfully!");
        } else {
            System.out.println("Operation cancelled.");
        }
    }

    // ==================== COURSE MANAGEMENT ====================

    private static void handleCourseManagement() {
        boolean back = false;

        while (!back) {
            printCourseMenu();
            int choice = getIntInput("Enter your choice: ");

            try {
                switch (choice) {
                    case MenuOptions.COURSE_ADD:
                        addCourse();
                        break;
                    case MenuOptions.COURSE_VIEW_ALL:
                        viewAllCourses();
                        break;
                    case MenuOptions.COURSE_SEARCH:
                        searchCourse();
                        break;
                    case MenuOptions.COURSE_UPDATE:
                        updateCourse();
                        break;
                    case MenuOptions.COURSE_TOGGLE_STATUS:
                        toggleCourseStatus();
                        break;
                    case MenuOptions.COURSE_BACK:
                        back = true;
                        break;
                    default:
                        System.out.println(AppConstants.INVALID_INPUT_MSG);
                }
            } catch (EntityNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (InvalidInputException e) {
                System.out.println("Validation Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private static void printCourseMenu() {
        System.out.println("\n----- Course Management -----");
        System.out.println("1. Add New Course");
        System.out.println("2. View All Courses");
        System.out.println("3. Search Course by ID");
        System.out.println("4. Update Course");
        System.out.println("5. Toggle Course Status");
        System.out.println("0. Back to Main Menu");
        System.out.println("-----------------------------");
    }

    private static void addCourse() throws InvalidInputException {
        System.out.println("\n--- Add New Course ---");

        String courseName = getStringInput("Enter Course Name: ");
        String description = getStringInput("Enter Description: ");
        int duration = getIntInput("Enter Duration (in weeks): ");

        Course course = courseService.addCourse(courseName, description, duration);
        System.out.println("\n✓ Course added successfully!");
        System.out.println("Course ID: " + course.getId());
        System.out.println(course);
    }

    private static void viewAllCourses() {
        System.out.println("\n--- All Courses ---");
        ArrayList<Course> courses = courseService.getAllCourses();

        if (courses.isEmpty()) {
            System.out.println("No courses found.");
        } else {
            System.out.println("Total Courses: " + courses.size());
            System.out.println("---------------------------------------------------");
            for (Course course : courses) {
                System.out.println(course);
            }
        }
    }

    private static void searchCourse() throws EntityNotFoundException {
        System.out.println("\n--- Search Course ---");
        int id = getIntInput("Enter Course ID: ");

        Course course = courseService.findCourseById(id);
        System.out.println("\nCourse Found:");
        System.out.println(course);
    }

    private static void updateCourse() throws EntityNotFoundException, InvalidInputException {
        System.out.println("\n--- Update Course ---");
        int id = getIntInput("Enter Course ID: ");

        // First, show current details
        Course course = courseService.findCourseById(id);
        System.out.println("\nCurrent Details:");
        System.out.println(course);

        System.out.println("\nEnter New Details:");
        String courseName = getStringInput("Enter Course Name: ");
        String description = getStringInput("Enter Description: ");
        int duration = getIntInput("Enter Duration (in weeks): ");

        courseService.updateCourse(id, courseName, description, duration);
        System.out.println("\n✓ Course updated successfully!");
    }

    private static void toggleCourseStatus() throws EntityNotFoundException {
        System.out.println("\n--- Toggle Course Status ---");
        int id = getIntInput("Enter Course ID: ");

        Course course = courseService.findCourseById(id);
        System.out.println("\nCurrent Status: " + course.getStatus().getDisplayName());

        courseService.toggleCourseStatus(id);

        course = courseService.findCourseById(id);
        System.out.println("New Status: " + course.getStatus().getDisplayName());
        System.out.println("\n✓ Course status toggled successfully!");
    }

    // ==================== ENROLLMENT MANAGEMENT ====================

    private static void handleEnrollmentManagement() {
        boolean back = false;

        while (!back) {
            printEnrollmentMenu();
            int choice = getIntInput("Enter your choice: ");

            try {
                switch (choice) {
                    case MenuOptions.ENROLLMENT_ENROLL:
                        enrollStudent();
                        break;
                    case MenuOptions.ENROLLMENT_VIEW_BY_STUDENT:
                        viewEnrollmentsByStudent();
                        break;
                    case MenuOptions.ENROLLMENT_VIEW_BY_COURSE:
                        viewEnrollmentsByCourse();
                        break;
                    case MenuOptions.ENROLLMENT_UPDATE_STATUS:
                        updateEnrollmentStatus();
                        break;
                    case MenuOptions.ENROLLMENT_VIEW_ALL:
                        viewAllEnrollments();
                        break;
                    case MenuOptions.ENROLLMENT_BACK:
                        back = true;
                        break;
                    default:
                        System.out.println(AppConstants.INVALID_INPUT_MSG);
                }
            } catch (EntityNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (InvalidInputException e) {
                System.out.println("Validation Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private static void printEnrollmentMenu() {
        System.out.println("\n----- Enrollment Management -----");
        System.out.println("1. Enroll Student in Course");
        System.out.println("2. View Enrollments by Student");
        System.out.println("3. View Enrollments by Course");
        System.out.println("4. Update Enrollment Status");
        System.out.println("5. View All Enrollments");
        System.out.println("0. Back to Main Menu");
        System.out.println("---------------------------------");
    }

    private static void enrollStudent() throws EntityNotFoundException, InvalidInputException {
        System.out.println("\n--- Enroll Student in Course ---");

        int studentId = getIntInput("Enter Student ID: ");
        int courseId = getIntInput("Enter Course ID: ");

        // Show student and course details
        Student student = studentService.findStudentById(studentId);
        Course course = courseService.findCourseById(courseId);

        System.out.println("\nStudent: " + student.getDisplayName());
        System.out.println("Course: " + course.getCourseName());

        String confirm = getStringInput("Confirm enrollment? (yes/no): ");
        if (confirm.equalsIgnoreCase("yes")) {
            Enrollment enrollment = enrollmentService.enrollStudent(studentId, courseId);
            System.out.println("\n✓ Student enrolled successfully!");
            System.out.println("Enrollment ID: " + enrollment.getId());
            System.out.println(enrollment);
        } else {
            System.out.println("Enrollment cancelled.");
        }
    }

    private static void viewEnrollmentsByStudent() throws EntityNotFoundException {
        System.out.println("\n--- View Enrollments by Student ---");
        int studentId = getIntInput("Enter Student ID: ");

        Student student = studentService.findStudentById(studentId);
        System.out.println("\nStudent: " + student.getDisplayName());

        ArrayList<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);

        if (enrollments.isEmpty()) {
            System.out.println("No enrollments found for this student.");
        } else {
            System.out.println("Total Enrollments: " + enrollments.size());
            System.out.println("---------------------------------------------------");
            for (Enrollment enrollment : enrollments) {
                System.out.println(enrollment);
                // Show course name
                try {
                    Course course = courseService.findCourseById(enrollment.getCourseId());
                    System.out.println("  Course: " + course.getCourseName());
                } catch (EntityNotFoundException e) {
                    System.out.println("  Course: [Not Found]");
                }
            }
        }
    }

    private static void viewEnrollmentsByCourse() throws EntityNotFoundException {
        System.out.println("\n--- View Enrollments by Course ---");
        int courseId = getIntInput("Enter Course ID: ");

        Course course = courseService.findCourseById(courseId);
        System.out.println("\nCourse: " + course.getCourseName());

        ArrayList<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);

        if (enrollments.isEmpty()) {
            System.out.println("No enrollments found for this course.");
        } else {
            System.out.println("Total Enrollments: " + enrollments.size());
            System.out.println("---------------------------------------------------");
            for (Enrollment enrollment : enrollments) {
                System.out.println(enrollment);
                // Show student name
                try {
                    Student student = studentService.findStudentById(enrollment.getStudentId());
                    System.out.println("  Student: " + student.getDisplayName());
                } catch (EntityNotFoundException e) {
                    System.out.println("  Student: [Not Found]");
                }
            }
        }
    }

    private static void updateEnrollmentStatus() throws EntityNotFoundException {
        System.out.println("\n--- Update Enrollment Status ---");
        int enrollmentId = getIntInput("Enter Enrollment ID: ");

        Enrollment enrollment = enrollmentService.findEnrollmentById(enrollmentId);
        System.out.println("\nCurrent Enrollment:");
        System.out.println(enrollment);
        System.out.println("Current Status: " + enrollment.getStatus().getDisplayName());

        System.out.println("\nSelect New Status:");
        System.out.println("1. ACTIVE");
        System.out.println("2. COMPLETED");
        System.out.println("3. CANCELLED");

        int statusChoice = getIntInput("Enter choice: ");

        EnrollmentStatus newStatus;
        switch (statusChoice) {
            case 1:
                newStatus = EnrollmentStatus.ACTIVE;
                break;
            case 2:
                newStatus = EnrollmentStatus.COMPLETED;
                break;
            case 3:
                newStatus = EnrollmentStatus.CANCELLED;
                break;
            default:
                System.out.println("Invalid status choice.");
                return;
        }

        enrollmentService.updateEnrollmentStatus(enrollmentId, newStatus);
        System.out.println("\n✓ Enrollment status updated to: " + newStatus.getDisplayName());
    }

    private static void viewAllEnrollments() {
        System.out.println("\n--- All Enrollments ---");
        ArrayList<Enrollment> enrollments = enrollmentService.getAllEnrollments();

        if (enrollments.isEmpty()) {
            System.out.println("No enrollments found.");
        } else {
            System.out.println("Total Enrollments: " + enrollments.size());
            System.out.println("---------------------------------------------------");
            for (Enrollment enrollment : enrollments) {
                System.out.println(enrollment);
            }
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Gets integer input from user with exception handling
     */
    private static int getIntInput(String message) {
        System.out.print(message);
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return -1;
        }
    }

    /**
     * Gets string input from user
     */
    private static String getStringInput(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
