package com.library.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Book class.
 */
class BookTest {

    @Test
    void testValidBookCreation() {
        Book book = new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2017);
        
        assertEquals("978-0134685991", book.getIsbn());
        assertEquals("Effective Java", book.getTitle());
        assertEquals("Joshua Bloch", book.getAuthor());
        assertEquals(2017, book.getPublicationYear());
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    @Test
    void testInvalidISBN() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Book("invalid-isbn", "Test Book", "Test Author", 2020);
        });
    }

    @Test
    void testNullTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Book("978-0134685991", null, "Test Author", 2020);
        });
    }

    @Test
    void testEmptyAuthor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Book("978-0134685991", "Test Book", "", 2020);
        });
    }

    @Test
    void testInvalidPublicationYear() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Book("978-0134685991", "Test Book", "Test Author", 3000);
        });
    }

    @Test
    void testBookEquality() {
        Book book1 = new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2017);
        Book book2 = new Book("978-0134685991", "Different Title", "Different Author", 2020);
        
        assertEquals(book1, book2); // Same ISBN means same book
    }

    @Test
    void testBookToString() {
        Book book = new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2017);
        String str = book.toString();
        
        assertTrue(str.contains("978-0134685991"));
        assertTrue(str.contains("Effective Java"));
        assertTrue(str.contains("Joshua Bloch"));
    }

    @Test
    void testSetTitle() {
        Book book = new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2017);
        book.setTitle("New Title");
        
        assertEquals("New Title", book.getTitle());
    }

    @Test
    void testSetInvalidTitle() {
        Book book = new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2017);
        
        assertThrows(IllegalArgumentException.class, () -> {
            book.setTitle("");
        });
    }

    @Test
    void testStatusChange() {
        Book book = new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2017);
        
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
        
        book.setStatus(BookStatus.CHECKED_OUT);
        assertEquals(BookStatus.CHECKED_OUT, book.getStatus());
    }

    @Test
    void testISBN10Format() {
        Book book = new Book("0134685997", "Test Book", "Test Author", 2020);
        assertEquals("0134685997", book.getIsbn());
    }

    @Test
    void testISBN13Format() {
        Book book = new Book("9780134685991", "Test Book", "Test Author", 2020);
        assertEquals("9780134685991", book.getIsbn());
    }
}

