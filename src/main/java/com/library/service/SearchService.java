package com.library.service;

import com.library.model.Book;
import com.library.model.BookStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for searching books in the library.
 * Demonstrates the Single Responsibility Principle.
 */
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final LibraryService libraryService;

    public SearchService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    /**
     * Search books by title (case-insensitive, partial match).
     */
    public List<Book> searchByTitle(String title) {
        logger.info("Searching books by title: {}", title);
        return libraryService.getAllBooks().stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Search books by author (case-insensitive, partial match).
     */
    public List<Book> searchByAuthor(String author) {
        logger.info("Searching books by author: {}", author);
        return libraryService.getAllBooks().stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Search book by ISBN (exact match).
     */
    public Book searchByISBN(String isbn) {
        logger.info("Searching book by ISBN: {}", isbn);
        return libraryService.getBook(isbn);
    }

    /**
     * Search books by publication year.
     */
    public List<Book> searchByYear(int year) {
        logger.info("Searching books by year: {}", year);
        return libraryService.getAllBooks().stream()
                .filter(book -> book.getPublicationYear() == year)
                .collect(Collectors.toList());
    }

    /**
     * Get all available books.
     */
    public List<Book> getAvailableBooks() {
        logger.info("Getting all available books");
        return libraryService.getAllBooks().stream()
                .filter(book -> book.getStatus() == BookStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    /**
     * Get all checked out books.
     */
    public List<Book> getCheckedOutBooks() {
        logger.info("Getting all checked out books");
        return libraryService.getAllBooks().stream()
                .filter(book -> book.getStatus() == BookStatus.CHECKED_OUT)
                .collect(Collectors.toList());
    }

    /**
     * Search books by multiple criteria.
     */
    public List<Book> advancedSearch(String title, String author, Integer year) {
        logger.info("Advanced search - title: {}, author: {}, year: {}", title, author, year);
        return libraryService.getAllBooks().stream()
                .filter(book -> {
                    boolean matches = true;
                    if (title != null && !title.isEmpty()) {
                        matches = book.getTitle().toLowerCase().contains(title.toLowerCase());
                    }
                    if (matches && author != null && !author.isEmpty()) {
                        matches = book.getAuthor().toLowerCase().contains(author.toLowerCase());
                    }
                    if (matches && year != null) {
                        matches = book.getPublicationYear() == year;
                    }
                    return matches;
                })
                .collect(Collectors.toList());
    }
}

