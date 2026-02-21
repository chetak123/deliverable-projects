package com.library.service;

import com.library.model.Book;
import com.library.model.LoanRecord;
import com.library.model.Patron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating book recommendations.
 * Uses patron borrowing history to suggest books.
 */
public class RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    
    private final LibraryService libraryService;

    public RecommendationService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    /**
     * Get book recommendations for a patron based on their borrowing history.
     */
    public List<Book> getRecommendations(String patronId, int maxRecommendations) {
        logger.info("Generating recommendations for patron {}", patronId);

        Patron patron = libraryService.getPatron(patronId);
        if (patron == null) {
            throw new IllegalArgumentException("Patron not found");
        }

        // Get patron's borrowing history
        List<LoanRecord> history = patron.getBorrowingHistory();
        if (history.isEmpty()) {
            logger.info("No borrowing history found, returning popular books");
            return getPopularBooks(maxRecommendations);
        }

        // Extract authors from borrowing history
        Set<String> favoriteAuthors = new HashSet<>();
        for (LoanRecord record : history) {
            Book book = libraryService.getBook(record.getIsbn());
            if (book != null) {
                favoriteAuthors.add(book.getAuthor());
            }
        }

        // Get books already borrowed
        Set<String> borrowedIsbns = history.stream()
                .map(LoanRecord::getIsbn)
                .collect(Collectors.toSet());

        // Find books by favorite authors that haven't been borrowed
        List<Book> recommendations = libraryService.getAllBooks().stream()
                .filter(book -> !borrowedIsbns.contains(book.getIsbn()))
                .filter(book -> favoriteAuthors.contains(book.getAuthor()))
                .limit(maxRecommendations)
                .collect(Collectors.toList());

        // If not enough recommendations, add popular books
        if (recommendations.size() < maxRecommendations) {
            int needed = maxRecommendations - recommendations.size();
            List<Book> popular = getPopularBooks(needed).stream()
                    .filter(book -> !borrowedIsbns.contains(book.getIsbn()))
                    .filter(book -> !recommendations.contains(book))
                    .collect(Collectors.toList());
            recommendations.addAll(popular);
        }

        logger.info("Generated {} recommendations for patron {}", 
            recommendations.size(), patronId);
        return recommendations;
    }

    /**
     * Get popular books (most borrowed).
     * This is a simplified implementation.
     */
    private List<Book> getPopularBooks(int count) {
        // In a real system, this would track borrow counts
        // For now, just return random available books
        return libraryService.getAllBooks().stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Get books by the same author.
     */
    public List<Book> getBooksByAuthor(String author) {
        return libraryService.getAllBooks().stream()
                .filter(book -> book.getAuthor().equalsIgnoreCase(author))
                .collect(Collectors.toList());
    }

    /**
     * Get books from the same publication year.
     */
    public List<Book> getBooksFromSameYear(int year) {
        return libraryService.getAllBooks().stream()
                .filter(book -> book.getPublicationYear() == year)
                .collect(Collectors.toList());
    }
}

