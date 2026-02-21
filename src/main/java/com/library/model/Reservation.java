package com.library.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a book reservation.
 */
public class Reservation {
    private final String reservationId;
    private final String isbn;
    private final String patronId;
    private final LocalDateTime reservationDate;
    private LocalDateTime expirationDate;
    private ReservationStatus status;

    public Reservation(String isbn, String patronId) {
        this.reservationId = UUID.randomUUID().toString();
        this.isbn = isbn;
        this.patronId = patronId;
        this.reservationDate = LocalDateTime.now();
        this.expirationDate = LocalDateTime.now().plusDays(3); // 3 days to pick up
        this.status = ReservationStatus.PENDING;
    }

    // Getters
    public String getReservationId() {
        return reservationId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPatronId() {
        return patronId;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    // Setters
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate) && status == ReservationStatus.READY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }

    @Override
    public String toString() {
        return String.format("Reservation{id='%s', isbn='%s', patronId='%s', status=%s}",
                reservationId, isbn, patronId, status);
    }
}

