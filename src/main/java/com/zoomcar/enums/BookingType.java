package com.zoomcar.enums;

/**
 * Booking Type enumeration
 */
public enum BookingType {
    HOURLY("hourly"),
    DAILY("daily"),
    WEEKLY("weekly");

    private final String value;

    BookingType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
