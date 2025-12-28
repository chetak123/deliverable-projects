package com.airtribe.learntrack.repository;

import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.enums.CourseStatus;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Repository for managing Course data in memory
 * Uses ArrayList to store courses
 */
public class CourseRepository {
    
    // ArrayList to store courses (in-memory storage)
    private final ArrayList<Course> courses;
    
    /**
     * Constructor initializes the ArrayList
     */
    public CourseRepository() {
        this.courses = new ArrayList<>();
    }
    
    /**
     * Adds a course to the repository
     * @param course the course to add
     */
    public void save(Course course) {
        courses.add(course);
    }
    
    /**
     * Finds a course by ID
     * @param id the course ID
     * @return Optional containing the course if found, empty otherwise
     */
    public Optional<Course> findById(int id) {
        for (Course course : courses) {
            if (course.getId() == id) {
                return Optional.of(course);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Returns all courses
     * @return ArrayList of all courses
     */
    public ArrayList<Course> findAll() {
        return new ArrayList<>(courses); // Return a copy to prevent external modification
    }
    
    /**
     * Returns all active courses
     * @return ArrayList of active courses
     */
    public ArrayList<Course> findAllActive() {
        ArrayList<Course> activeCourses = new ArrayList<>();
        for (Course course : courses) {
            if (course.getStatus() == CourseStatus.ACTIVE) {
                activeCourses.add(course);
            }
        }
        return activeCourses;
    }
    
    /**
     * Updates a course
     * @param updatedCourse the course with updated information
     * @return true if updated successfully, false if course not found
     */
    public boolean update(Course updatedCourse) {
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getId() == updatedCourse.getId()) {
                courses.set(i, updatedCourse);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Deletes a course by ID
     * @param id the course ID
     * @return true if deleted successfully, false if course not found
     */
    public boolean deleteById(int id) {
        return courses.removeIf(course -> course.getId() == id);
    }
    
    /**
     * Checks if a course exists by ID
     * @param id the course ID
     * @return true if exists, false otherwise
     */
    public boolean existsById(int id) {
        return findById(id).isPresent();
    }
    
    /**
     * Returns the count of courses
     * @return number of courses
     */
    public int count() {
        return courses.size();
    }
}

