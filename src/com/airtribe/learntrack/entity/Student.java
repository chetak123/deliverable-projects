package com.airtribe.learntrack.entity;

/**
 * Student entity extending Person
 * Demonstrates inheritance and constructor overloading
 */
public class Student extends Person {
    
    // Additional fields specific to Student
    private String batch;
    private boolean active;
    
    // Default constructor
    public Student() {
        super();
        this.active = true; // Students are active by default
    }
    
    // Parameterized constructor with all fields
    public Student(int id, String firstName, String lastName, String email, String batch) {
        super(id, firstName, lastName, email); // Call parent constructor
        this.batch = batch;
        this.active = true;
    }
    
    // Constructor without email (constructor overloading)
    public Student(int id, String firstName, String lastName, String batch) {
        super(id, firstName, lastName); // Call parent constructor without email
        this.batch = batch;
        this.active = true;
    }
    
    // Constructor with active status
    public Student(int id, String firstName, String lastName, String email, String batch, boolean active) {
        super(id, firstName, lastName, email);
        this.batch = batch;
        this.active = active;
    }
    
    // Getters and Setters
    public String getBatch() {
        return batch;
    }
    
    public void setBatch(String batch) {
        this.batch = batch;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Override getDisplayName to include batch information
     * Demonstrates method overriding (Polymorphism)
     */
    @Override
    public String getDisplayName() {
        return super.getDisplayName() + " (Batch: " + batch + ")";
    }
    
    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", name='" + getFirstName() + " " + getLastName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", batch='" + batch + '\'' +
                ", active=" + active +
                '}';
    }
}

