package com.zoomcar.exception;

import java.util.UUID;

/**
 * Exception thrown when a booking is not found
 */
public class BookingNotFoundException extends RuntimeException {
    
    private final UUID bookingId;
    private final String bookingReference;
    
    public BookingNotFoundException(UUID bookingId) {
        super("Booking not found with ID: " + bookingId);
        this.bookingId = bookingId;
        this.bookingReference = null;
    }
    
    public BookingNotFoundException(String bookingReference) {
        super("Booking not found with reference: " + bookingReference);
        this.bookingId = null;
        this.bookingReference = bookingReference;
    }
    
    public BookingNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.bookingId = null;
        this.bookingReference = null;
    }
    
    public UUID getBookingId() {
        return bookingId;
    }
    
    public String getBookingReference() {
        return bookingReference;
    }
}
