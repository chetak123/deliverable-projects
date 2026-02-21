package com.library.pattern.strategy;

import java.time.LocalDate;

/**
 * Strategy interface for different lending policies.
 * Implements the Strategy pattern.
 */
public interface LendingStrategy {
    /**
     * Calculate the due date for a book loan.
     * 
     * @param checkoutDate The date the book was checked out
     * @return The due date for the loan
     */
    LocalDate calculateDueDate(LocalDate checkoutDate);

    /**
     * Calculate the late fee for an overdue book.
     * 
     * @param daysOverdue The number of days the book is overdue
     * @return The late fee amount
     */
    double calculateLateFee(long daysOverdue);

    /**
     * Get the name of this lending strategy.
     * 
     * @return The strategy name
     */
    String getStrategyName();
}

