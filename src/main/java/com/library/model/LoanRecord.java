package com.library.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a loan record for tracking book borrowing history.
 */
public class LoanRecord {
    private final String isbn;
    private final String patronId;
    private final LocalDate checkoutDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;

    public LoanRecord(String isbn, String patronId, LocalDate checkoutDate, LocalDate dueDate) {
        this.isbn = isbn;
        this.patronId = patronId;
        this.checkoutDate = checkoutDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.status = LoanStatus.ACTIVE;
    }

    // Getters
    public String getIsbn() {
        return isbn;
    }

    public String getPatronId() {
        return patronId;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    // Setters
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE && LocalDate.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanRecord that = (LoanRecord) o;
        return Objects.equals(isbn, that.isbn) &&
               Objects.equals(patronId, that.patronId) &&
               Objects.equals(checkoutDate, that.checkoutDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn, patronId, checkoutDate);
    }

    @Override
    public String toString() {
        return String.format("LoanRecord{isbn='%s', patronId='%s', checkout=%s, due=%s, status=%s}",
                isbn, patronId, checkoutDate, dueDate, status);
    }
}

