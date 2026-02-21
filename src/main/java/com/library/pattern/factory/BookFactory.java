package com.library.pattern.factory;

import com.library.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating Book objects.
 * Implements the Factory pattern to encapsulate object creation.
 */
public class BookFactory {
    private static final Logger logger = LoggerFactory.getLogger(BookFactory.class);

    /**
     * Creates a new Book with validation.
     * 
     * @param isbn The book's ISBN
     * @param title The book's title
     * @param author The book's author
     * @param publicationYear The publication year
     * @return A new Book instance
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static Book createBook(String isbn, String title, String author, int publicationYear) {
        try {
            Book book = new Book(isbn, title, author, publicationYear);
            logger.info("Created new book: {}", book);
            return book;
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create book: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Creates a Book from a CSV-like string format.
     * Format: "ISBN,Title,Author,Year"
     * 
     * @param bookData The book data string
     * @return A new Book instance
     */
    public static Book createBookFromString(String bookData) {
        String[] parts = bookData.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid book data format. Expected: ISBN,Title,Author,Year");
        }
        
        String isbn = parts[0].trim();
        String title = parts[1].trim();
        String author = parts[2].trim();
        int year = Integer.parseInt(parts[3].trim());
        
        return createBook(isbn, title, author, year);
    }
}

