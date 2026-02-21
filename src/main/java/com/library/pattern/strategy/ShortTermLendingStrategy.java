package com.library.pattern.strategy;

import java.time.LocalDate;

/**
 * Short-term lending strategy with 7-day loan period.
 * Concrete implementation of the Strategy pattern.
 */
public class ShortTermLendingStrategy implements LendingStrategy {
    private static final int LOAN_PERIOD_DAYS = 7;
    private static final double DAILY_LATE_FEE = 1.00;

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
        return "Short-term (7 days)";
    }
}

