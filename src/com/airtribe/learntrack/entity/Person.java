package com.airtribe.learntrack.entity;

/**
 * Base class representing a Person
 * Demonstrates inheritance - Student and other entities can extend this
 */
public class Person {
    
    // Private fields for encapsulation
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    
    // Default constructor
    public Person() {
    }
    
    // Parameterized constructor
    public Person(int id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    // Constructor without email (constructor overloading)
    public Person(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = "";
    }
    
    // Getters and Setters (Encapsulation)
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Returns the full name of the person
     * This method can be overridden by subclasses (Polymorphism)
     * @return full name
     */
    public String getDisplayName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

