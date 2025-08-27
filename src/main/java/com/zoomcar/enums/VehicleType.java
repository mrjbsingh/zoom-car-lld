package com.zoomcar.enums;

/**
 * Vehicle Type enumeration
 */
public enum VehicleType {
    CAR("car"),
    BIKE("bike");

    private final String value;

    VehicleType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
