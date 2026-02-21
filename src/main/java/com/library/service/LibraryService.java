package com.library.service;

import com.library.model.*;
import com.library.pattern.observer.Observer;
import com.library.pattern.observer.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Main library service implementing the Singleton pattern.
 * Manages books, patrons, and branches.
 */
public class LibraryService implements Subject {
    private static final Logger logger = LoggerFactory.getLogger(LibraryService.class);
    private static LibraryService instance;

    private final Map<String, Book> books;
    private final Map<String, Patron> patrons;
    private final Map<String, Branch> branches;
    private final List<Observer> observers;

    /**
     * Private constructor for Singleton pattern.
     */
    private LibraryService() {
        this.books = new HashMap<>();
        this.patrons = new HashMap<>();
        this.branches = new HashMap<>();
        this.observers = new ArrayList<>();
        logger.info("LibraryService initialized");
    }

    /**
     * Get the singleton instance of LibraryService.
     * 
     * @return The LibraryService instance
     */
    public static synchronized LibraryService getInstance() {
        if (instance == null) {
            instance = new LibraryService();
        }
        return instance;
    }

    // Book Management
    public void addBook(Book book) {
        if (books.containsKey(book.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN " + book.getIsbn() + " already exists");
        }
        books.put(book.getIsbn(), book);
        logger.info("Added book: {}", book);
    }

    public void removeBook(String isbn) {
        Book removed = books.remove(isbn);
        if (removed != null) {
            logger.info("Removed book: {}", removed);
        }
    }

    public void updateBook(String isbn, Book updatedBook) {
        if (!books.containsKey(isbn)) {
            throw new IllegalArgumentException("Book with ISBN " + isbn + " not found");
        }
        books.put(isbn, updatedBook);
        logger.info("Updated book: {}", updatedBook);
    }

    public Book getBook(String isbn) {
        return books.get(isbn);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    // Patron Management
    public void addPatron(Patron patron) {
        if (patrons.containsKey(patron.getPatronId())) {
            throw new IllegalArgumentException("Patron with ID " + patron.getPatronId() + " already exists");
        }
        patrons.put(patron.getPatronId(), patron);
        logger.info("Added patron: {}", patron);
    }

    public void removePatron(String patronId) {
        Patron removed = patrons.remove(patronId);
        if (removed != null) {
            logger.info("Removed patron: {}", removed);
        }
    }

    public void updatePatron(String patronId, Patron updatedPatron) {
        if (!patrons.containsKey(patronId)) {
            throw new IllegalArgumentException("Patron with ID " + patronId + " not found");
        }
        patrons.put(patronId, updatedPatron);
        logger.info("Updated patron: {}", updatedPatron);
    }

    public Patron getPatron(String patronId) {
        return patrons.get(patronId);
    }

    public List<Patron> getAllPatrons() {
        return new ArrayList<>(patrons.values());
    }

    // Branch Management
    public void addBranch(Branch branch) {
        if (branches.containsKey(branch.getBranchId())) {
            throw new IllegalArgumentException("Branch with ID " + branch.getBranchId() + " already exists");
        }
        branches.put(branch.getBranchId(), branch);
        logger.info("Added branch: {}", branch);
    }

    public void removeBranch(String branchId) {
        Branch removed = branches.remove(branchId);
        if (removed != null) {
            logger.info("Removed branch: {}", removed);
        }
    }

    public Branch getBranch(String branchId) {
        return branches.get(branchId);
    }

    public List<Branch> getAllBranches() {
        return new ArrayList<>(branches.values());
    }

    // Observer Pattern Implementation
    @Override
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            logger.debug("Attached observer: {}", observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
        logger.debug("Detached observer: {}", observer);
    }

    @Override
    public void notifyObservers(String event) {
        logger.debug("Notifying {} observers: {}", observers.size(), event);
        for (Observer observer : observers) {
            observer.update(event);
        }
    }
}

