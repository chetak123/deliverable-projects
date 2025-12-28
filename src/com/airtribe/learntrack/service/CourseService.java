package com.airtribe.learntrack.service;

import com.airtribe.learntrack.entity.Course;
import com.airtribe.learntrack.enums.CourseStatus;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;
import com.airtribe.learntrack.repository.CourseRepository;
import com.airtribe.learntrack.util.IdGenerator;
import com.airtribe.learntrack.util.InputValidator;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Service class for Course business logic
 */
public class CourseService {
    
    private final CourseRepository courseRepository;
    
    /**
     * Constructor initializes the repository
     */
    public CourseService() {
        this.courseRepository = new CourseRepository();
    }
    
    /**
     * Adds a new course
     * @param courseName course name
     * @param description course description
     * @param durationInWeeks course duration in weeks
     * @return the created course
     * @throws InvalidInputException if input validation fails
     */
    public Course addCourse(String courseName, String description, int durationInWeeks) 
            throws InvalidInputException {
        
        // Validate inputs
        if (!InputValidator.isValidName(courseName)) {
            throw new InvalidInputException("Invalid course name. Must be 2-50 characters.");
        }
        if (!InputValidator.isNotEmpty(description)) {
            throw new InvalidInputException("Description cannot be empty.");
        }
        if (!InputValidator.isValidDuration(durationInWeeks)) {
            throw new InvalidInputException("Invalid duration. Must be between 1 and 52 weeks.");
        }
        
        // Generate ID and create course
        int id = IdGenerator.getNextCourseId();
        Course course = new Course(id, courseName, description, durationInWeeks);
        
        // Save to repository
        courseRepository.save(course);
        
        return course;
    }
    
    /**
     * Finds a course by ID
     * @param id the course ID
     * @return the course
     * @throws EntityNotFoundException if course not found
     */
    public Course findCourseById(int id) throws EntityNotFoundException {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isPresent()) {
            return course.get();
        }
        throw new EntityNotFoundException("Course with ID " + id + " not found.");
    }
    
    /**
     * Returns all courses
     * @return ArrayList of all courses
     */
    public ArrayList<Course> getAllCourses() {
        return courseRepository.findAll();
    }
    
    /**
     * Returns all active courses
     * @return ArrayList of active courses
     */
    public ArrayList<Course> getActiveCourses() {
        return courseRepository.findAllActive();
    }
    
    /**
     * Updates a course's information
     * @param id course ID
     * @param courseName new course name
     * @param description new description
     * @param durationInWeeks new duration
     * @throws EntityNotFoundException if course not found
     * @throws InvalidInputException if input validation fails
     */
    public void updateCourse(int id, String courseName, String description, int durationInWeeks) 
            throws EntityNotFoundException, InvalidInputException {
        
        // Find existing course
        Course course = findCourseById(id);
        
        // Validate inputs
        if (!InputValidator.isValidName(courseName)) {
            throw new InvalidInputException("Invalid course name. Must be 2-50 characters.");
        }
        if (!InputValidator.isNotEmpty(description)) {
            throw new InvalidInputException("Description cannot be empty.");
        }
        if (!InputValidator.isValidDuration(durationInWeeks)) {
            throw new InvalidInputException("Invalid duration. Must be between 1 and 52 weeks.");
        }
        
        // Update course
        course.setCourseName(courseName);
        course.setDescription(description);
        course.setDurationInWeeks(durationInWeeks);
        
        courseRepository.update(course);
    }
    
    /**
     * Toggles course status between ACTIVE and INACTIVE
     * @param id the course ID
     * @throws EntityNotFoundException if course not found
     */
    public void toggleCourseStatus(int id) throws EntityNotFoundException {
        Course course = findCourseById(id);
        course.toggleStatus();
        courseRepository.update(course);
    }
    
    /**
     * Sets course status
     * @param id the course ID
     * @param status the new status
     * @throws EntityNotFoundException if course not found
     */
    public void setCourseStatus(int id, CourseStatus status) throws EntityNotFoundException {
        Course course = findCourseById(id);
        course.setStatus(status);
        courseRepository.update(course);
    }
}

