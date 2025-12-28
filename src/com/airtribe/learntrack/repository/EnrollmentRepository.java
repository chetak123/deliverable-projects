package com.airtribe.learntrack.repository;

import com.airtribe.learntrack.entity.Enrollment;
import com.airtribe.learntrack.enums.EnrollmentStatus;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Repository for managing Enrollment data in memory
 * Uses ArrayList to store enrollments
 */
public class EnrollmentRepository {
    
    // ArrayList to store enrollments (in-memory storage)
    private final ArrayList<Enrollment> enrollments;
    
    /**
     * Constructor initializes the ArrayList
     */
    public EnrollmentRepository() {
        this.enrollments = new ArrayList<>();
    }
    
    /**
     * Adds an enrollment to the repository
     * @param enrollment the enrollment to add
     */
    public void save(Enrollment enrollment) {
        enrollments.add(enrollment);
    }
    
    /**
     * Finds an enrollment by ID
     * @param id the enrollment ID
     * @return Optional containing the enrollment if found, empty otherwise
     */
    public Optional<Enrollment> findById(int id) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getId() == id) {
                return Optional.of(enrollment);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Returns all enrollments
     * @return ArrayList of all enrollments
     */
    public ArrayList<Enrollment> findAll() {
        return new ArrayList<>(enrollments); // Return a copy to prevent external modification
    }
    
    /**
     * Finds all enrollments for a specific student
     * @param studentId the student ID
     * @return ArrayList of enrollments for the student
     */
    public ArrayList<Enrollment> findByStudentId(int studentId) {
        ArrayList<Enrollment> studentEnrollments = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudentId() == studentId) {
                studentEnrollments.add(enrollment);
            }
        }
        return studentEnrollments;
    }
    
    /**
     * Finds all enrollments for a specific course
     * @param courseId the course ID
     * @return ArrayList of enrollments for the course
     */
    public ArrayList<Enrollment> findByCourseId(int courseId) {
        ArrayList<Enrollment> courseEnrollments = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCourseId() == courseId) {
                courseEnrollments.add(enrollment);
            }
        }
        return courseEnrollments;
    }
    
    /**
     * Checks if a student is already enrolled in a course
     * @param studentId the student ID
     * @param courseId the course ID
     * @return true if enrolled, false otherwise
     */
    public boolean existsByStudentIdAndCourseId(int studentId, int courseId) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudentId() == studentId && 
                enrollment.getCourseId() == courseId &&
                enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Updates an enrollment
     * @param updatedEnrollment the enrollment with updated information
     * @return true if updated successfully, false if enrollment not found
     */
    public boolean update(Enrollment updatedEnrollment) {
        for (int i = 0; i < enrollments.size(); i++) {
            if (enrollments.get(i).getId() == updatedEnrollment.getId()) {
                enrollments.set(i, updatedEnrollment);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Deletes an enrollment by ID
     * @param id the enrollment ID
     * @return true if deleted successfully, false if enrollment not found
     */
    public boolean deleteById(int id) {
        return enrollments.removeIf(enrollment -> enrollment.getId() == id);
    }
    
    /**
     * Returns the count of enrollments
     * @return number of enrollments
     */
    public int count() {
        return enrollments.size();
    }
}

