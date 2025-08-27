package com.zoomcar.enums;

/**
 * Fuel Type enumeration
 */
public enum FuelType {
    PETROL("petrol"),
    DIESEL("diesel"),
    ELECTRIC("electric"),
    HYBRID("hybrid");

    private final String value;

    FuelType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
