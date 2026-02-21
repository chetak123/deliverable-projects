package com.library.service;

import com.library.model.*;
import com.library.pattern.strategy.LendingStrategy;
import com.library.pattern.strategy.StandardLendingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

/**
 * Service for managing book lending operations.
 * Demonstrates the Strategy pattern and Single Responsibility Principle.
 */
public class LendingService {
    private static final Logger logger = LoggerFactory.getLogger(LendingService.class);
    
    private final LibraryService libraryService;
    private final Map<String, LoanRecord> activeLoans;
    private LendingStrategy lendingStrategy;

    public LendingService(LibraryService libraryService) {
        this.libraryService = libraryService;
        this.activeLoans = new HashMap<>();
        this.lendingStrategy = new StandardLendingStrategy();
    }

    /**
     * Set the lending strategy (Strategy pattern).
     */
    public void setLendingStrategy(LendingStrategy strategy) {
        this.lendingStrategy = strategy;
        logger.info("Lending strategy changed to: {}", strategy.getStrategyName());
    }

    /**
     * Checkout a book to a patron.
     */
    public LoanRecord checkoutBook(String isbn, String patronId) {
        logger.info("Attempting to checkout book {} to patron {}", isbn, patronId);
        
        // Validate book exists and is available
        Book book = libraryService.getBook(isbn);
        if (book == null) {
            throw new IllegalArgumentException("Book with ISBN " + isbn + " not found");
        }
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new IllegalStateException("Book is not available for checkout");
        }

        // Validate patron exists and can borrow more books
        Patron patron = libraryService.getPatron(patronId);
        if (patron == null) {
            throw new IllegalArgumentException("Patron with ID " + patronId + " not found");
        }
        if (!patron.canBorrowMoreBooks()) {
            throw new IllegalStateException("Patron has reached maximum book limit");
        }

        // Create loan record
        LocalDate checkoutDate = LocalDate.now();
        LocalDate dueDate = lendingStrategy.calculateDueDate(checkoutDate);
        LoanRecord loan = new LoanRecord(isbn, patronId, checkoutDate, dueDate);

        // Update book status
        book.setStatus(BookStatus.CHECKED_OUT);

        // Update patron
        patron.addLoan(isbn);
        patron.addToBorrowingHistory(loan);

        // Track active loan
        activeLoans.put(isbn, loan);

        logger.info("Book checked out successfully: {}", loan);
        libraryService.notifyObservers(
            String.format("Book '%s' checked out to %s. Due date: %s", 
                book.getTitle(), patron.getName(), dueDate)
        );

        return loan;
    }

    /**
     * Return a book.
     */
    public void returnBook(String isbn, String patronId) {
        logger.info("Attempting to return book {} from patron {}", isbn, patronId);

        // Validate book exists
        Book book = libraryService.getBook(isbn);
        if (book == null) {
            throw new IllegalArgumentException("Book with ISBN " + isbn + " not found");
        }

        // Validate patron exists and has the book
        Patron patron = libraryService.getPatron(patronId);
        if (patron == null) {
            throw new IllegalArgumentException("Patron with ID " + patronId + " not found");
        }
        if (!patron.hasBook(isbn)) {
            throw new IllegalStateException("Patron does not have this book checked out");
        }

        // Get loan record
        LoanRecord loan = activeLoans.get(isbn);
        if (loan == null) {
            throw new IllegalStateException("No active loan found for this book");
        }

        // Update loan record
        loan.setReturnDate(LocalDate.now());
        loan.setStatus(loan.isOverdue() ? LoanStatus.OVERDUE : LoanStatus.RETURNED);

        // Update book status
        book.setStatus(BookStatus.AVAILABLE);

        // Update patron
        patron.removeLoan(isbn);

        // Remove from active loans
        activeLoans.remove(isbn);

        // Calculate late fee if overdue
        double lateFee = 0;
        if (loan.isOverdue()) {
            lateFee = lendingStrategy.calculateLateFee(loan.getDaysOverdue());
            logger.warn("Book returned late. Days overdue: {}, Late fee: ${}", 
                loan.getDaysOverdue(), lateFee);
        }

        logger.info("Book returned successfully: {}", loan);
        libraryService.notifyObservers(
            String.format("Book '%s' returned by %s%s", 
                book.getTitle(), patron.getName(),
                lateFee > 0 ? String.format(". Late fee: $%.2f", lateFee) : "")
        );
    }

    /**
     * Get all active loans.
     */
    public List<LoanRecord> getActiveLoans() {
        return new ArrayList<>(activeLoans.values());
    }

    /**
     * Get active loans for a specific patron.
     */
    public List<LoanRecord> getPatronLoans(String patronId) {
        return activeLoans.values().stream()
                .filter(loan -> loan.getPatronId().equals(patronId))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get overdue loans.
     */
    public List<LoanRecord> getOverdueLoans() {
        return activeLoans.values().stream()
                .filter(LoanRecord::isOverdue)
                .collect(java.util.stream.Collectors.toList());
    }
}

