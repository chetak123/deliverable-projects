package com.library.service;

import com.library.model.*;
import com.library.pattern.factory.BookFactory;
import com.library.pattern.factory.PatronFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the LendingService class.
 */
class LendingServiceTest {

    private static int testCounter = 0;
    private LendingService lendingService;
    private Book testBook;
    private Patron testPatron;

    @BeforeEach
    void setUp() {
        // Create new lending service for each test
        LibraryService libraryService = LibraryService.getInstance();
        lendingService = new LendingService(libraryService);

        // Create test data with unique ISBN for each test
        testCounter++;
        String uniqueIsbn = String.format("978013468%04d", testCounter);
        testBook = BookFactory.createBook(uniqueIsbn, "Effective Java", "Joshua Bloch", 2017);
        testPatron = PatronFactory.createPatron("Test User", "test@email.com", "555-0000");

        libraryService.addBook(testBook);
        libraryService.addPatron(testPatron);
    }

    @Test
    void testSuccessfulCheckout() {
        LoanRecord loan = lendingService.checkoutBook(testBook.getIsbn(), testPatron.getPatronId());
        
        assertNotNull(loan);
        assertEquals(testBook.getIsbn(), loan.getIsbn());
        assertEquals(testPatron.getPatronId(), loan.getPatronId());
        assertEquals(BookStatus.CHECKED_OUT, testBook.getStatus());
        assertTrue(testPatron.hasBook(testBook.getIsbn()));
    }

    @Test
    void testCheckoutNonexistentBook() {
        assertThrows(IllegalArgumentException.class, () -> {
            lendingService.checkoutBook("invalid-isbn", testPatron.getPatronId());
        });
    }

    @Test
    void testCheckoutToNonexistentPatron() {
        assertThrows(IllegalArgumentException.class, () -> {
            lendingService.checkoutBook(testBook.getIsbn(), "invalid-patron-id");
        });
    }

    @Test
    void testCheckoutUnavailableBook() {
        testBook.setStatus(BookStatus.CHECKED_OUT);
        
        assertThrows(IllegalStateException.class, () -> {
            lendingService.checkoutBook(testBook.getIsbn(), testPatron.getPatronId());
        });
    }

    @Test
    void testCheckoutWhenPatronAtLimit() {
        // Set patron limit to 1
        testPatron.setMaxBooksAllowed(1);

        // Checkout first book
        lendingService.checkoutBook(testBook.getIsbn(), testPatron.getPatronId());

        // Try to checkout another book
        testCounter++;
        String uniqueIsbn = String.format("978013468%04d", testCounter);
        Book anotherBook = BookFactory.createBook(uniqueIsbn, "Head First Design Patterns",
                                                   "Eric Freeman", 2004);
        LibraryService.getInstance().addBook(anotherBook);

        assertThrows(IllegalStateException.class, () -> {
            lendingService.checkoutBook(anotherBook.getIsbn(), testPatron.getPatronId());
        });
    }

    @Test
    void testSuccessfulReturn() {
        // First checkout
        lendingService.checkoutBook(testBook.getIsbn(), testPatron.getPatronId());
        
        // Then return
        lendingService.returnBook(testBook.getIsbn(), testPatron.getPatronId());
        
        assertEquals(BookStatus.AVAILABLE, testBook.getStatus());
        assertFalse(testPatron.hasBook(testBook.getIsbn()));
    }

    @Test
    void testReturnBookNotBorrowed() {
        assertThrows(IllegalStateException.class, () -> {
            lendingService.returnBook(testBook.getIsbn(), testPatron.getPatronId());
        });
    }

    @Test
    void testGetActiveLoans() {
        lendingService.checkoutBook(testBook.getIsbn(), testPatron.getPatronId());
        
        assertEquals(1, lendingService.getActiveLoans().size());
    }

    @Test
    void testGetPatronLoans() {
        lendingService.checkoutBook(testBook.getIsbn(), testPatron.getPatronId());
        
        assertEquals(1, lendingService.getPatronLoans(testPatron.getPatronId()).size());
    }
}

