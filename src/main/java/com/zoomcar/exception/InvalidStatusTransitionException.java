package com.zoomcar.exception;

import com.zoomcar.enums.BookingStatus;
import java.util.UUID;

/**
 * Exception thrown when an invalid booking status transition is attempted
 */
public class InvalidStatusTransitionException extends RuntimeException {
    
    private final UUID bookingId;
    private final BookingStatus currentStatus;
    private final BookingStatus targetStatus;
    
    public InvalidStatusTransitionException(UUID bookingId, BookingStatus currentStatus, BookingStatus targetStatus) {
        super(String.format("Invalid status transition for booking %s: %s -> %s", 
                           bookingId, currentStatus, targetStatus));
        this.bookingId = bookingId;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
    
    public InvalidStatusTransitionException(String message) {
        super(message);
        this.bookingId = null;
        this.currentStatus = null;
        this.targetStatus = null;
    }
    
    public UUID getBookingId() {
        return bookingId;
    }
    
    public BookingStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public BookingStatus getTargetStatus() {
        return targetStatus;
    }
}
