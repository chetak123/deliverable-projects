package com.airtribe.learntrack.service;

import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.entity.Enrollment;
import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.enums.EnrollmentStatus;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;
import com.airtribe.learntrack.repository.EnrollmentRepository;
import com.airtribe.learntrack.util.IdGenerator;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Service class for Enrollment business logic
 */
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;
    private final CourseService courseService;
    
    /**
     * Constructor initializes the repository and dependent services
     */
    public EnrollmentService(StudentService studentService, CourseService courseService) {
        this.enrollmentRepository = new EnrollmentRepository();
        this.studentService = studentService;
        this.courseService = courseService;
    }
    
    /**
     * Enrolls a student in a course
     * @param studentId the student ID
     * @param courseId the course ID
     * @return the created enrollment
     * @throws EntityNotFoundException if student or course not found
     * @throws InvalidInputException if student is already enrolled
     */
    public Enrollment enrollStudent(int studentId, int courseId) 
            throws EntityNotFoundException, InvalidInputException {
        
        // Verify student exists and is active
        Student student = studentService.findStudentById(studentId);
        if (!student.isActive()) {
            throw new InvalidInputException("Cannot enroll inactive student.");
        }
        
        // Verify course exists
        Course course = courseService.findCourseById(courseId);
        
        // Check if already enrolled
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new InvalidInputException("Student is already enrolled in this course.");
        }
        
        // Create enrollment
        int id = IdGenerator.getNextEnrollmentId();
        Enrollment enrollment = new Enrollment(id, studentId, courseId);
        
        // Save to repository
        enrollmentRepository.save(enrollment);
        
        return enrollment;
    }
    
    /**
     * Finds an enrollment by ID
     * @param id the enrollment ID
     * @return the enrollment
     * @throws EntityNotFoundException if enrollment not found
     */
    public Enrollment findEnrollmentById(int id) throws EntityNotFoundException {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
        if (enrollment.isPresent()) {
            return enrollment.get();
        }
        throw new EntityNotFoundException("Enrollment with ID " + id + " not found.");
    }
    
    /**
     * Returns all enrollments
     * @return ArrayList of all enrollments
     */
    public ArrayList<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }
    
    /**
     * Returns all enrollments for a specific student
     * @param studentId the student ID
     * @return ArrayList of enrollments
     * @throws EntityNotFoundException if student not found
     */
    public ArrayList<Enrollment> getEnrollmentsByStudent(int studentId) throws EntityNotFoundException {
        // Verify student exists
        studentService.findStudentById(studentId);
        return enrollmentRepository.findByStudentId(studentId);
    }
    
    /**
     * Returns all enrollments for a specific course
     * @param courseId the course ID
     * @return ArrayList of enrollments
     * @throws EntityNotFoundException if course not found
     */
    public ArrayList<Enrollment> getEnrollmentsByCourse(int courseId) throws EntityNotFoundException {
        // Verify course exists
        courseService.findCourseById(courseId);
        return enrollmentRepository.findByCourseId(courseId);
    }
    
    /**
     * Updates enrollment status
     * @param id the enrollment ID
     * @param status the new status
     * @throws EntityNotFoundException if enrollment not found
     */
    public void updateEnrollmentStatus(int id, EnrollmentStatus status) throws EntityNotFoundException {
        Enrollment enrollment = findEnrollmentById(id);
        enrollment.setStatus(status);
        enrollmentRepository.update(enrollment);
    }
    
    /**
     * Marks an enrollment as completed
     * @param id the enrollment ID
     * @throws EntityNotFoundException if enrollment not found
     */
    public void completeEnrollment(int id) throws EntityNotFoundException {
        updateEnrollmentStatus(id, EnrollmentStatus.COMPLETED);
    }
    
    /**
     * Cancels an enrollment
     * @param id the enrollment ID
     * @throws EntityNotFoundException if enrollment not found
     */
    public void cancelEnrollment(int id) throws EntityNotFoundException {
        updateEnrollmentStatus(id, EnrollmentStatus.CANCELLED);
    }
}

