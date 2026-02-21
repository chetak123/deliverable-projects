package com.library.service;

import com.library.model.*;
import com.library.pattern.observer.PatronObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing book reservations.
 * Implements the Observer pattern for notifications.
 */
public class ReservationService {
    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);
    
    private final LibraryService libraryService;
    private final Map<String, Queue<Reservation>> reservationQueues; // ISBN -> Queue of reservations
    private final Map<String, Reservation> reservations; // ReservationId -> Reservation

    public ReservationService(LibraryService libraryService) {
        this.libraryService = libraryService;
        this.reservationQueues = new HashMap<>();
        this.reservations = new HashMap<>();
    }

    /**
     * Reserve a book for a patron.
     */
    public Reservation reserveBook(String isbn, String patronId) {
        logger.info("Attempting to reserve book {} for patron {}", isbn, patronId);

        // Validate book exists
        Book book = libraryService.getBook(isbn);
        if (book == null) {
            throw new IllegalArgumentException("Book with ISBN " + isbn + " not found");
        }

        // Validate patron exists
        Patron patron = libraryService.getPatron(patronId);
        if (patron == null) {
            throw new IllegalArgumentException("Patron with ID " + patronId + " not found");
        }

        // Check if patron already has a reservation for this book
        Queue<Reservation> queue = reservationQueues.get(isbn);
        if (queue != null) {
            boolean alreadyReserved = queue.stream()
                    .anyMatch(r -> r.getPatronId().equals(patronId) && 
                                   r.getStatus() == ReservationStatus.PENDING);
            if (alreadyReserved) {
                throw new IllegalStateException("Patron already has a reservation for this book");
            }
        }

        // Create reservation
        Reservation reservation = new Reservation(isbn, patronId);
        reservations.put(reservation.getReservationId(), reservation);

        // Add to queue
        reservationQueues.computeIfAbsent(isbn, k -> new LinkedList<>()).add(reservation);

        // Update patron
        patron.addReservation(isbn);

        logger.info("Book reserved successfully: {}", reservation);
        
        // Notify patron
        PatronObserver observer = new PatronObserver(patron);
        observer.update(String.format("You have reserved '%s'. You are #%d in the queue.", 
            book.getTitle(), reservationQueues.get(isbn).size()));

        return reservation;
    }

    /**
     * Cancel a reservation.
     */
    public void cancelReservation(String reservationId) {
        logger.info("Attempting to cancel reservation {}", reservationId);

        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        // Remove from queue
        Queue<Reservation> queue = reservationQueues.get(reservation.getIsbn());
        if (queue != null) {
            queue.remove(reservation);
        }

        // Update patron
        Patron patron = libraryService.getPatron(reservation.getPatronId());
        if (patron != null) {
            patron.removeReservation(reservation.getIsbn());
        }

        logger.info("Reservation cancelled: {}", reservation);
    }

    /**
     * Notify next patron in queue when book becomes available.
     */
    public void notifyNextInQueue(String isbn) {
        logger.info("Notifying next patron in queue for book {}", isbn);

        Queue<Reservation> queue = reservationQueues.get(isbn);
        if (queue == null || queue.isEmpty()) {
            logger.debug("No reservations in queue for book {}", isbn);
            return;
        }

        Reservation nextReservation = queue.peek();
        if (nextReservation != null) {
            nextReservation.setStatus(ReservationStatus.READY);
            
            Book book = libraryService.getBook(isbn);
            Patron patron = libraryService.getPatron(nextReservation.getPatronId());
            
            if (book != null && patron != null) {
                book.setStatus(BookStatus.RESERVED);
                
                PatronObserver observer = new PatronObserver(patron);
                observer.update(String.format("Your reserved book '%s' is now available! " +
                    "Please pick it up within 3 days.", book.getTitle()));
                
                logger.info("Notified patron {} about available book {}", 
                    patron.getName(), book.getTitle());
            }
        }
    }

    /**
     * Fulfill a reservation (patron picks up the book).
     */
    public void fulfillReservation(String reservationId) {
        logger.info("Fulfilling reservation {}", reservationId);

        Reservation reservation = reservations.get(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found");
        }

        if (reservation.getStatus() != ReservationStatus.READY) {
            throw new IllegalStateException("Reservation is not ready for pickup");
        }

        reservation.setStatus(ReservationStatus.FULFILLED);

        // Remove from queue
        Queue<Reservation> queue = reservationQueues.get(reservation.getIsbn());
        if (queue != null) {
            queue.poll(); // Remove the first (this) reservation
        }

        // Update patron
        Patron patron = libraryService.getPatron(reservation.getPatronId());
        if (patron != null) {
            patron.removeReservation(reservation.getIsbn());
        }

        logger.info("Reservation fulfilled: {}", reservation);
    }

    /**
     * Get all reservations for a book.
     */
    public List<Reservation> getBookReservations(String isbn) {
        Queue<Reservation> queue = reservationQueues.get(isbn);
        return queue != null ? new ArrayList<>(queue) : new ArrayList<>();
    }

    /**
     * Get all reservations for a patron.
     */
    public List<Reservation> getPatronReservations(String patronId) {
        return reservations.values().stream()
                .filter(r -> r.getPatronId().equals(patronId))
                .filter(r -> r.getStatus() == ReservationStatus.PENDING || 
                            r.getStatus() == ReservationStatus.READY)
                .collect(Collectors.toList());
    }
}

