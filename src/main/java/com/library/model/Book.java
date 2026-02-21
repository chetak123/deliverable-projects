package com.library.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a book in the library system.
 * Demonstrates encapsulation and proper validation.
 */
public class Book {
    private static final Pattern ISBN_10_PATTERN = Pattern.compile("^\\d{9}[\\dX]$");
    private static final Pattern ISBN_13_PATTERN = Pattern.compile("^\\d{13}$");
    
    private final String isbn;
    private String title;
    private String author;
    private int publicationYear;
    private BookStatus status;
    private String currentBranchId;

    /**
     * Constructor for creating a new Book.
     * 
     * @param isbn The unique ISBN identifier
     * @param title The book title
     * @param author The book author
     * @param publicationYear The year of publication
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Book(String isbn, String title, String author, int publicationYear) {
        validateISBN(isbn);
        validateTitle(title);
        validateAuthor(author);
        validatePublicationYear(publicationYear);
        
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.status = BookStatus.AVAILABLE;
        this.currentBranchId = null;
    }

    /**
     * Validates ISBN format (ISBN-10 or ISBN-13).
     */
    private void validateISBN(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        
        String cleanIsbn = isbn.replaceAll("-", "").trim();
        
        if (!ISBN_10_PATTERN.matcher(cleanIsbn).matches() && 
            !ISBN_13_PATTERN.matcher(cleanIsbn).matches()) {
            throw new IllegalArgumentException("Invalid ISBN format. Must be ISBN-10 or ISBN-13");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
    }

    private void validateAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }
    }

    private void validatePublicationYear(int year) {
        int currentYear = java.time.Year.now().getValue();
        if (year < 1000 || year > currentYear) {
            throw new IllegalArgumentException("Invalid publication year: " + year);
        }
    }

    // Getters
    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public BookStatus getStatus() {
        return status;
    }

    public String getCurrentBranchId() {
        return currentBranchId;
    }

    // Setters with validation
    public void setTitle(String title) {
        validateTitle(title);
        this.title = title;
    }

    public void setAuthor(String author) {
        validateAuthor(author);
        this.author = author;
    }

    public void setPublicationYear(int publicationYear) {
        validatePublicationYear(publicationYear);
        this.publicationYear = publicationYear;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public void setCurrentBranchId(String branchId) {
        this.currentBranchId = branchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return String.format("Book{isbn='%s', title='%s', author='%s', year=%d, status=%s}",
                isbn, title, author, publicationYear, status);
    }
}

