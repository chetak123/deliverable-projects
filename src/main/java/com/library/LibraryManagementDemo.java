package com.library;

import com.library.model.*;
import com.library.pattern.factory.BookFactory;
import com.library.pattern.factory.PatronFactory;
import com.library.pattern.observer.PatronObserver;
import com.library.pattern.strategy.ExtendedLendingStrategy;
import com.library.pattern.strategy.ShortTermLendingStrategy;
import com.library.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Demonstration of the Library Management System.
 * Shows all major features and design patterns in action.
 */
public class LibraryManagementDemo {
    private static final Logger logger = LoggerFactory.getLogger(LibraryManagementDemo.class);

    public static void main(String[] args) {
        logger.info("Starting Library Management System Demo");
        System.out.println("=== Library Management System Demo ===\n");

        // Get singleton instance
        LibraryService libraryService = LibraryService.getInstance();
        
        // Initialize services
        SearchService searchService = new SearchService(libraryService);
        LendingService lendingService = new LendingService(libraryService);
        ReservationService reservationService = new ReservationService(libraryService);
        BranchService branchService = new BranchService(libraryService);
        RecommendationService recommendationService = new RecommendationService(libraryService);

        // Demo 1: Create books using Factory pattern
        System.out.println("--- Creating Books (Factory Pattern) ---");
        Book book1 = BookFactory.createBook("978-0134685991", "Effective Java", "Joshua Bloch", 2017);
        Book book2 = BookFactory.createBook("978-0596009205", "Head First Design Patterns", "Eric Freeman", 2004);
        Book book3 = BookFactory.createBook("978-0132350884", "Clean Code", "Robert Martin", 2008);
        Book book4 = BookFactory.createBook("978-0201633610", "Design Patterns", "Gang of Four", 1994);
        
        libraryService.addBook(book1);
        libraryService.addBook(book2);
        libraryService.addBook(book3);
        libraryService.addBook(book4);
        System.out.println("✓ Added 4 books to library\n");

        // Demo 2: Create patrons using Factory pattern
        System.out.println("--- Creating Patrons (Factory Pattern) ---");
        Patron patron1 = PatronFactory.createPatron("Alice Johnson", "alice@email.com", "555-0101");
        Patron patron2 = PatronFactory.createPatron("Bob Smith", "bob@email.com", "555-0102");
        
        libraryService.addPatron(patron1);
        libraryService.addPatron(patron2);
        System.out.println("✓ Added 2 patrons to library\n");

        // Demo 3: Observer pattern - attach observers
        System.out.println("--- Setting up Notifications (Observer Pattern) ---");
        PatronObserver observer1 = new PatronObserver(patron1);
        PatronObserver observer2 = new PatronObserver(patron2);
        libraryService.attach(observer1);
        libraryService.attach(observer2);
        System.out.println("✓ Observers attached\n");

        // Demo 4: Search functionality
        System.out.println("--- Searching Books ---");
        List<Book> javaBooks = searchService.searchByTitle("Java");
        System.out.println("Books with 'Java' in title: " + javaBooks.size());
        javaBooks.forEach(b -> System.out.println("  - " + b.getTitle()));
        System.out.println();

        // Demo 5: Checkout book (Strategy pattern)
        System.out.println("--- Checking Out Books (Strategy Pattern) ---");
        System.out.println("Using Standard Lending Strategy (14 days)");
        LoanRecord loan1 = lendingService.checkoutBook(book1.getIsbn(), patron1.getPatronId());
        System.out.println("✓ Loan created: Due date = " + loan1.getDueDate() + "\n");

        // Demo 6: Change lending strategy
        System.out.println("--- Changing Lending Strategy ---");
        lendingService.setLendingStrategy(new ExtendedLendingStrategy());
        LoanRecord loan2 = lendingService.checkoutBook(book2.getIsbn(), patron1.getPatronId());
        System.out.println("✓ Using Extended Strategy (30 days): Due date = " + loan2.getDueDate() + "\n");

        // Demo 7: Reservation system
        System.out.println("--- Book Reservation System ---");
        Reservation reservation = reservationService.reserveBook(book1.getIsbn(), patron2.getPatronId());
        System.out.println("✓ Book reserved: " + reservation + "\n");

        // Demo 8: Multi-branch support
        System.out.println("--- Multi-Branch Support ---");
        Branch mainBranch = new Branch("Main Library", "123 Main St");
        Branch eastBranch = new Branch("East Branch", "456 East Ave");
        
        libraryService.addBranch(mainBranch);
        libraryService.addBranch(eastBranch);
        
        mainBranch.addBook(book3);
        mainBranch.addBook(book4);
        
        System.out.println("✓ Created 2 branches");
        System.out.println("Main Branch inventory: " + mainBranch.getAllBooks().size() + " books");
        
        branchService.transferBook(book3.getIsbn(), mainBranch.getBranchId(), eastBranch.getBranchId());
        System.out.println("✓ Transferred book between branches");
        System.out.println("East Branch inventory: " + eastBranch.getAllBooks().size() + " books\n");

        // Demo 9: Return book and notify reservations
        System.out.println("--- Returning Book and Notifications ---");
        lendingService.returnBook(book1.getIsbn(), patron1.getPatronId());
        reservationService.notifyNextInQueue(book1.getIsbn());
        System.out.println();

        // Demo 10: Recommendations
        System.out.println("--- Book Recommendations ---");
        List<Book> recommendations = recommendationService.getRecommendations(patron1.getPatronId(), 3);
        System.out.println("Recommendations for " + patron1.getName() + ":");
        recommendations.forEach(b -> System.out.println("  - " + b.getTitle() + " by " + b.getAuthor()));
        System.out.println();

        // Demo 11: View patron borrowing history
        System.out.println("--- Patron Borrowing History ---");
        System.out.println(patron1.getName() + "'s history:");
        patron1.getBorrowingHistory().forEach(record -> 
            System.out.println("  - " + libraryService.getBook(record.getIsbn()).getTitle() + 
                             " (Status: " + record.getStatus() + ")"));
        System.out.println();

        System.out.println("=== Demo Complete ===");
        logger.info("Library Management System Demo completed successfully");
    }
}

