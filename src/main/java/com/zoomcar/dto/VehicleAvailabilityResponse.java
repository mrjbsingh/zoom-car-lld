package com.zoomcar.dto;

import com.zoomcar.enums.VehicleType;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for vehicle availability queries
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleAvailabilityResponse {
    
    private UUID categoryId;
    private String categoryName;
    private VehicleType vehicleType;
    private String brand;
    private String modelName;
    private BigDecimal pricePerHour;
    private Integer availableCount;
    private Integer earliestSlot;
    private List<AvailableSlot> availableSlots;
    private List<String> features;
    private String imageUrl;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AvailableSlot {
        private Integer hourSlot;
        private LocalDateTime slotTime;
        private Integer availableCount;
        private List<UUID> availableVehicleIds;
    }
}
