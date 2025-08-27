package com.zoomcar.exception;

import com.zoomcar.enums.BookingStatus;
import java.util.UUID;

/**
 * Exception thrown when booking cancellation is not allowed
 */
public class BookingCancellationNotAllowedException extends RuntimeException {
    
    private final UUID bookingId;
    private final BookingStatus currentStatus;
    private final String reason;
    
    public BookingCancellationNotAllowedException(UUID bookingId, BookingStatus currentStatus, String reason) {
        super(String.format("Booking %s cannot be cancelled. Current status: %s. Reason: %s", 
                           bookingId, currentStatus, reason));
        this.bookingId = bookingId;
        this.currentStatus = currentStatus;
        this.reason = reason;
    }
    
    public BookingCancellationNotAllowedException(String message) {
        super(message);
        this.bookingId = null;
        this.currentStatus = null;
        this.reason = message;
    }
    
    public UUID getBookingId() {
        return bookingId;
    }
    
    public BookingStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public String getReason() {
        return reason;
    }
}
