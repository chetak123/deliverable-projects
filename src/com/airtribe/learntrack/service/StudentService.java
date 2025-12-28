package com.airtribe.learntrack.service;

import com.airtribe.learntrack.entity.Student;
import com.airtribe.learntrack.exception.EntityNotFoundException;
import com.airtribe.learntrack.exception.InvalidInputException;
import com.airtribe.learntrack.repository.StudentRepository;
import com.airtribe.learntrack.util.IdGenerator;
import com.airtribe.learntrack.util.InputValidator;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Service class for Student business logic
 * Demonstrates separation of concerns - business logic separate from data storage
 */
public class StudentService {
    
    private final StudentRepository studentRepository;
    
    /**
     * Constructor initializes the repository
     */
    public StudentService() {
        this.studentRepository = new StudentRepository();
    }
    
    /**
     * Adds a new student
     * @param firstName student's first name
     * @param lastName student's last name
     * @param email student's email
     * @param batch student's batch
     * @return the created student
     * @throws InvalidInputException if input validation fails
     */
    public Student addStudent(String firstName, String lastName, String email, String batch) 
            throws InvalidInputException {
        
        // Validate inputs
        if (!InputValidator.isValidName(firstName)) {
            throw new InvalidInputException("Invalid first name. Must be 2-50 characters.");
        }
        if (!InputValidator.isValidName(lastName)) {
            throw new InvalidInputException("Invalid last name. Must be 2-50 characters.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new InvalidInputException("Invalid email format.");
        }
        if (!InputValidator.isNotEmpty(batch)) {
            throw new InvalidInputException("Batch cannot be empty.");
        }
        
        // Generate ID and create student
        int id = IdGenerator.getNextStudentId();
        Student student = new Student(id, firstName, lastName, email, batch);
        
        // Save to repository
        studentRepository.save(student);
        
        return student;
    }
    
    /**
     * Finds a student by ID
     * @param id the student ID
     * @return the student
     * @throws EntityNotFoundException if student not found
     */
    public Student findStudentById(int id) throws EntityNotFoundException {
        Optional<Student> student = studentRepository.findById(id);
        if (student.isPresent()) {
            return student.get();
        }
        throw new EntityNotFoundException("Student with ID " + id + " not found.");
    }
    
    /**
     * Returns all students
     * @return ArrayList of all students
     */
    public ArrayList<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    
    /**
     * Returns all active students
     * @return ArrayList of active students
     */
    public ArrayList<Student> getActiveStudents() {
        return studentRepository.findAllActive();
    }
    
    /**
     * Updates a student's information
     * @param id student ID
     * @param firstName new first name
     * @param lastName new last name
     * @param email new email
     * @param batch new batch
     * @throws EntityNotFoundException if student not found
     * @throws InvalidInputException if input validation fails
     */
    public void updateStudent(int id, String firstName, String lastName, String email, String batch) 
            throws EntityNotFoundException, InvalidInputException {
        
        // Find existing student
        Student student = findStudentById(id);
        
        // Validate inputs
        if (!InputValidator.isValidName(firstName)) {
            throw new InvalidInputException("Invalid first name. Must be 2-50 characters.");
        }
        if (!InputValidator.isValidName(lastName)) {
            throw new InvalidInputException("Invalid last name. Must be 2-50 characters.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new InvalidInputException("Invalid email format.");
        }
        if (!InputValidator.isNotEmpty(batch)) {
            throw new InvalidInputException("Batch cannot be empty.");
        }
        
        // Update student
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setBatch(batch);
        
        studentRepository.update(student);
    }
    
    /**
     * Deactivates a student (soft delete)
     * @param id the student ID
     * @throws EntityNotFoundException if student not found
     */
    public void deactivateStudent(int id) throws EntityNotFoundException {
        Student student = findStudentById(id);
        student.setActive(false);
        studentRepository.update(student);
    }
    
    /**
     * Activates a student
     * @param id the student ID
     * @throws EntityNotFoundException if student not found
     */
    public void activateStudent(int id) throws EntityNotFoundException {
        Student student = findStudentById(id);
        student.setActive(true);
        studentRepository.update(student);
    }
}

