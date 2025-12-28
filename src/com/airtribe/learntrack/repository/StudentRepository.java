package com.airtribe.learntrack.repository;

import com.airtribe.learntrack.entity.Student;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Repository for managing Student data in memory
 * Uses ArrayList to store students
 */
public class StudentRepository {
    
    // ArrayList to store students (in-memory storage)
    private final ArrayList<Student> students;
    
    /**
     * Constructor initializes the ArrayList
     */
    public StudentRepository() {
        this.students = new ArrayList<>();
    }
    
    /**
     * Adds a student to the repository
     * @param student the student to add
     */
    public void save(Student student) {
        students.add(student);
    }
    
    /**
     * Finds a student by ID
     * @param id the student ID
     * @return Optional containing the student if found, empty otherwise
     */
    public Optional<Student> findById(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return Optional.of(student);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Returns all students
     * @return ArrayList of all students
     */
    public ArrayList<Student> findAll() {
        return new ArrayList<>(students); // Return a copy to prevent external modification
    }
    
    /**
     * Returns all active students
     * @return ArrayList of active students
     */
    public ArrayList<Student> findAllActive() {
        ArrayList<Student> activeStudents = new ArrayList<>();
        for (Student student : students) {
            if (student.isActive()) {
                activeStudents.add(student);
            }
        }
        return activeStudents;
    }
    
    /**
     * Updates a student
     * @param updatedStudent the student with updated information
     * @return true if updated successfully, false if student not found
     */
    public boolean update(Student updatedStudent) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId() == updatedStudent.getId()) {
                students.set(i, updatedStudent);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Deletes a student by ID
     * @param id the student ID
     * @return true if deleted successfully, false if student not found
     */
    public boolean deleteById(int id) {
        return students.removeIf(student -> student.getId() == id);
    }
    
    /**
     * Checks if a student exists by ID
     * @param id the student ID
     * @return true if exists, false otherwise
     */
    public boolean existsById(int id) {
        return findById(id).isPresent();
    }
    
    /**
     * Returns the count of students
     * @return number of students
     */
    public int count() {
        return students.size();
    }
}

