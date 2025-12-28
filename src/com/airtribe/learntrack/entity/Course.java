package com.airtribe.learntrack.entity;

import com.airtribe.learntrack.enums.CourseStatus;

/**
 * Course entity
 * Demonstrates encapsulation with private fields and public getters/setters
 */
public class Course {
    
    // Private fields for encapsulation
    private int id;
    private String courseName;
    private String description;
    private int durationInWeeks;
    private CourseStatus status;
    
    // Default constructor
    public Course() {
        this.status = CourseStatus.ACTIVE; // Courses are active by default
    }
    
    // Parameterized constructor
    public Course(int id, String courseName, String description, int durationInWeeks) {
        this.id = id;
        this.courseName = courseName;
        this.description = description;
        this.durationInWeeks = durationInWeeks;
        this.status = CourseStatus.ACTIVE;
    }
    
    // Constructor with status (constructor overloading)
    public Course(int id, String courseName, String description, int durationInWeeks, CourseStatus status) {
        this.id = id;
        this.courseName = courseName;
        this.description = description;
        this.durationInWeeks = durationInWeeks;
        this.status = status;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getDurationInWeeks() {
        return durationInWeeks;
    }
    
    public void setDurationInWeeks(int durationInWeeks) {
        this.durationInWeeks = durationInWeeks;
    }
    
    public CourseStatus getStatus() {
        return status;
    }
    
    public void setStatus(CourseStatus status) {
        this.status = status;
    }
    
    /**
     * Toggles the course status between ACTIVE and INACTIVE
     */
    public void toggleStatus() {
        this.status = (this.status == CourseStatus.ACTIVE) ? CourseStatus.INACTIVE : CourseStatus.ACTIVE;
    }
    
    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", courseName='" + courseName + '\'' +
                ", description='" + description + '\'' +
                ", durationInWeeks=" + durationInWeeks +
                ", status=" + status.getDisplayName() +
                '}';
    }
}

