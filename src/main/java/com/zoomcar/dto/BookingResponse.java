package com.zoomcar.dto;

import com.zoomcar.enums.BookingStatus;
import com.zoomcar.enums.BookingType;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for booking operations
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    
    private UUID bookingId;
    private String bookingReference;
    private UUID userId;
    private String userName;
    private UUID vehicleId;
    private String vehicleRegistrationNumber;
    private String vehicleModel;
    private String vehicleBrand;
    private String categoryName;
    private UUID pickupHubId;
    private String pickupHubName;
    private String pickupHubAddress;
    private UUID dropHubId;
    private String dropHubName;
    private String dropHubAddress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingType bookingType;
    private BookingStatus status;
    private BigDecimal baseAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String specialRequests;
    private String promoCode;
    private BigDecimal discountAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
