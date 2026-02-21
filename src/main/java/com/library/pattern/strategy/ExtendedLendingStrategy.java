package com.library.pattern.strategy;

import java.time.LocalDate;

/**
 * Extended lending strategy with 30-day loan period.
 * Concrete implementation of the Strategy pattern.
 */
public class ExtendedLendingStrategy implements LendingStrategy {
    private static final int LOAN_PERIOD_DAYS = 30;
    private static final double DAILY_LATE_FEE = 0.25;

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
        return "Extended (30 days)";
    }
}

