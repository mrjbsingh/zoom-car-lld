package com.zoomcar.enums;

/**
 * Transmission Type enumeration
 */
public enum TransmissionType {
    MANUAL("manual"),
    AUTOMATIC("automatic");

    private final String value;

    TransmissionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
