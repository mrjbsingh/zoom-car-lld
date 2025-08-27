package com.zoomcar.enums;

/**
 * Booking Status enumeration
 */
public enum BookingStatus {
    CONFIRMED("confirmed"),
    ACTIVE("active"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    BookingStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
