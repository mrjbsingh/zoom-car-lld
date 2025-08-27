package com.zoomcar.exception;

/**
 * Exception thrown when a booking conflict occurs (e.g., vehicle no longer available)
 */
public class BookingConflictException extends RuntimeException {
    
    private final String conflictReason;
    private final String vehicleId;
    
    public BookingConflictException(String message) {
        super(message);
        this.conflictReason = message;
        this.vehicleId = null;
    }
    
    public BookingConflictException(String message, String vehicleId) {
        super(message);
        this.conflictReason = message;
        this.vehicleId = vehicleId;
    }
    
    public BookingConflictException(String message, String vehicleId, Throwable cause) {
        super(message, cause);
        this.conflictReason = message;
        this.vehicleId = vehicleId;
    }
    
    public String getConflictReason() {
        return conflictReason;
    }
    
    public String getVehicleId() {
        return vehicleId;
    }
}
