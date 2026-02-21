package com.library.pattern.strategy;

import java.time.LocalDate;

/**
 * Standard lending strategy with 14-day loan period.
 * Concrete implementation of the Strategy pattern.
 */
public class StandardLendingStrategy implements LendingStrategy {
    private static final int LOAN_PERIOD_DAYS = 14;
    private static final double DAILY_LATE_FEE = 0.50;

    @Override
    public LocalDate calculateDueDate(LocalDate checkoutDate) {
        return checkoutDate.plusDays(LOAN_PERIOD_DAYS);
    }

    @Override
    public double calculateLateFee(long daysOverdue) {
        return daysOverdue * DAILY_LATE_FEE;
    }

    @Override
    public String getStrategyName() {
        return "Standard (14 days)";
    }
}

