package com.smartparking.enums;

/**
 * Enum representing the status of a parking transaction.
 */
public enum TransactionStatus {
    ACTIVE,      // Vehicle is currently parked
    COMPLETED,   // Vehicle has exited and payment is done
    PENDING,     // Payment is pending
    CANCELLED    // Transaction was cancelled
}

