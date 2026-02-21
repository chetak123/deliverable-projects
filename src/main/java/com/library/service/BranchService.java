package com.library.service;

import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.model.Branch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Service for managing multiple library branches.
 * Demonstrates the Single Responsibility Principle.
 */
public class BranchService {
    private static final Logger logger = LoggerFactory.getLogger(BranchService.class);
    
    private final LibraryService libraryService;

    public BranchService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    /**
     * Transfer a book from one branch to another.
     */
    public void transferBook(String isbn, String fromBranchId, String toBranchId) {
        logger.info("Transferring book {} from branch {} to branch {}", 
            isbn, fromBranchId, toBranchId);

        // Validate branches exist
        Branch fromBranch = libraryService.getBranch(fromBranchId);
        Branch toBranch = libraryService.getBranch(toBranchId);
        
        if (fromBranch == null) {
            throw new IllegalArgumentException("Source branch not found");
        }
        if (toBranch == null) {
            throw new IllegalArgumentException("Destination branch not found");
        }

        // Validate book exists in source branch
        Book book = fromBranch.getBook(isbn);
        if (book == null) {
            throw new IllegalArgumentException("Book not found in source branch");
        }

        // Validate book is available for transfer
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new IllegalStateException("Book is not available for transfer");
        }

        // Perform transfer
        book.setStatus(BookStatus.IN_TRANSIT);
        fromBranch.removeBook(isbn);
        
        // Simulate transfer completion
        book.setStatus(BookStatus.AVAILABLE);
        toBranch.addBook(book);

        logger.info("Book transferred successfully from {} to {}", 
            fromBranch.getName(), toBranch.getName());
        
        libraryService.notifyObservers(
            String.format("Book '%s' transferred from %s to %s", 
                book.getTitle(), fromBranch.getName(), toBranch.getName())
        );
    }

    /**
     * Get all books in a specific branch.
     */
    public List<Book> getBranchInventory(String branchId) {
        Branch branch = libraryService.getBranch(branchId);
        if (branch == null) {
            throw new IllegalArgumentException("Branch not found");
        }
        return branch.getAllBooks();
    }

    /**
     * Get available books in a specific branch.
     */
    public List<Book> getAvailableBooksInBranch(String branchId) {
        Branch branch = libraryService.getBranch(branchId);
        if (branch == null) {
            throw new IllegalArgumentException("Branch not found");
        }
        return branch.getAvailableBooks();
    }

    /**
     * Find which branch has a specific book.
     */
    public Branch findBookLocation(String isbn) {
        for (Branch branch : libraryService.getAllBranches()) {
            if (branch.hasBook(isbn)) {
                return branch;
            }
        }
        return null;
    }
}

