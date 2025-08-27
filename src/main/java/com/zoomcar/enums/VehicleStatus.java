package com.zoomcar.enums;

/**
 * Vehicle Status enumeration
 */
public enum VehicleStatus {
    AVAILABLE("available"),
    BOOKED("booked"),
    IN_USE("in_use"),
    MAINTENANCE("maintenance"),
    INACTIVE("inactive");

    private final String value;

    VehicleStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
