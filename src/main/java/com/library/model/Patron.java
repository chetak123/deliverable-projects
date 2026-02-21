package com.library.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a library patron (member).
 * Demonstrates encapsulation and proper data management.
 */
public class Patron {
    private final String patronId;
    private String name;
    private String email;
    private String phoneNumber;
    private final List<LoanRecord> borrowingHistory;
    private final List<String> currentLoans;
    private final List<String> reservations;
    private int maxBooksAllowed;

    /**
     * Constructor for creating a new Patron.
     * 
     * @param name The patron's name
     * @param email The patron's email
     * @param phoneNumber The patron's phone number
     */
    public Patron(String name, String email, String phoneNumber) {
        this.patronId = UUID.randomUUID().toString();
        validateName(name);
        validateEmail(email);
        
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.borrowingHistory = new ArrayList<>();
        this.currentLoans = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.maxBooksAllowed = 5; // Default limit
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        // Basic email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // Getters
    public String getPatronId() {
        return patronId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public List<LoanRecord> getBorrowingHistory() {
        return Collections.unmodifiableList(borrowingHistory);
    }

    public List<String> getCurrentLoans() {
        return Collections.unmodifiableList(currentLoans);
    }

    public List<String> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public int getMaxBooksAllowed() {
        return maxBooksAllowed;
    }

    // Setters
    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    public void setEmail(String email) {
        validateEmail(email);
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setMaxBooksAllowed(int maxBooksAllowed) {
        if (maxBooksAllowed < 1) {
            throw new IllegalArgumentException("Max books allowed must be at least 1");
        }
        this.maxBooksAllowed = maxBooksAllowed;
    }

    // Business methods
    public void addLoan(String isbn) {
        if (!currentLoans.contains(isbn)) {
            currentLoans.add(isbn);
        }
    }

    public void removeLoan(String isbn) {
        currentLoans.remove(isbn);
    }

    public void addToBorrowingHistory(LoanRecord record) {
        borrowingHistory.add(record);
    }

    public void addReservation(String isbn) {
        if (!reservations.contains(isbn)) {
            reservations.add(isbn);
        }
    }

    public void removeReservation(String isbn) {
        reservations.remove(isbn);
    }

    public boolean canBorrowMoreBooks() {
        return currentLoans.size() < maxBooksAllowed;
    }

    public boolean hasBook(String isbn) {
        return currentLoans.contains(isbn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patron patron = (Patron) o;
        return Objects.equals(patronId, patron.patronId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patronId);
    }

    @Override
    public String toString() {
        return String.format("Patron{id='%s', name='%s', email='%s', currentLoans=%d}",
                patronId, name, email, currentLoans.size());
    }
}

