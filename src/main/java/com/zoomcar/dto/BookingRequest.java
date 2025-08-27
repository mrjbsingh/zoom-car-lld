package com.zoomcar.dto;

import com.zoomcar.enums.BookingType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for creating a new booking
 */
@Data
public class BookingRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;
    
    @NotNull(message = "Pickup hub ID is required")
    private UUID pickupHubId;
    
    @NotNull(message = "Drop hub ID is required")
    private UUID dropHubId;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;
    
    @NotNull(message = "Booking type is required")
    private BookingType bookingType;
    
    // Optional fields
    private String specialRequests;
    private String promoCode;
}
