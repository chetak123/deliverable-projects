package com.smartparking.enums;

/**
 * Enum representing different parking spot allocation strategies.
 */
public enum AllocationStrategy {
    FIRST_AVAILABLE,        // Allocate the first available spot
    NEAREST_TO_ENTRANCE,    // Allocate spot closest to entrance
    NEAREST_TO_ELEVATOR,    // Allocate spot closest to elevator
    BEST_FIT               // Allocate the best matching spot size
}

