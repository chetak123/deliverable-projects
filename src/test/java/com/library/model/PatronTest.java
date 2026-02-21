package com.library.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Patron class.
 */
class PatronTest {

    @Test
    void testValidPatronCreation() {
        Patron patron = new Patron("John Doe", "john@email.com", "555-1234");
        
        assertNotNull(patron.getPatronId());
        assertEquals("John Doe", patron.getName());
        assertEquals("john@email.com", patron.getEmail());
        assertEquals("555-1234", patron.getPhoneNumber());
        assertEquals(5, patron.getMaxBooksAllowed());
        assertTrue(patron.getCurrentLoans().isEmpty());
    }

    @Test
    void testInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Patron("John Doe", "invalid-email", "555-1234");
        });
    }

    @Test
    void testNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Patron(null, "john@email.com", "555-1234");
        });
    }

    @Test
    void testAddLoan() {
        Patron patron = new Patron("John Doe", "john@email.com", "555-1234");
        patron.addLoan("978-0134685991");
        
        assertEquals(1, patron.getCurrentLoans().size());
        assertTrue(patron.hasBook("978-0134685991"));
    }

    @Test
    void testRemoveLoan() {
        Patron patron = new Patron("John Doe", "john@email.com", "555-1234");
        patron.addLoan("978-0134685991");
        patron.removeLoan("978-0134685991");
        
        assertEquals(0, patron.getCurrentLoans().size());
        assertFalse(patron.hasBook("978-0134685991"));
    }

    @Test
    void testCanBorrowMoreBooks() {
        Patron patron = new Patron("John Doe", "john@email.com", "555-1234");
        
        assertTrue(patron.canBorrowMoreBooks());
        
        // Add max books
        for (int i = 0; i < 5; i++) {
            patron.addLoan("ISBN-" + i);
        }
        
        assertFalse(patron.canBorrowMoreBooks());
    }

    @Test
    void testSetMaxBooksAllowed() {
        Patron patron = new Patron("John Doe", "john@email.com", "555-1234");
        patron.setMaxBooksAllowed(10);
        
        assertEquals(10, patron.getMaxBooksAllowed());
    }

    @Test
    void testSetInvalidMaxBooks() {
        Patron patron = new Patron("John Doe", "john@email.com", "555-1234");
        
        assertThrows(IllegalArgumentException.class, () -> {
            patron.setMaxBooksAllowed(0);
        });
    }

    @Test
    void testAddReservation() {
        Patron patron = new Patron("John Doe", "john@email.com", "555-1234");
        patron.addReservation("978-0134685991");
        
        assertEquals(1, patron.getReservations().size());
    }

    @Test
    void testRemoveReservation() {
        Patron patron = new Patron("John Doe", "john@email.com", "555-1234");
        patron.addReservation("978-0134685991");
        patron.removeReservation("978-0134685991");
        
        assertEquals(0, patron.getReservations().size());
    }

    @Test
    void testPatronEquality() {
        Patron patron1 = new Patron("John Doe", "john@email.com", "555-1234");
        Patron patron2 = patron1;
        
        assertEquals(patron1, patron2);
    }

    @Test
    void testUpdateEmail() {
        Patron patron = new Patron("John Doe", "john@email.com", "555-1234");
        patron.setEmail("newemail@email.com");
        
        assertEquals("newemail@email.com", patron.getEmail());
    }
}

