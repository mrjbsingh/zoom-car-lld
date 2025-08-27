package com.zoomcar.controller;

import com.zoomcar.dto.VehicleAvailabilityResponse;
import com.zoomcar.enums.VehicleType;
import com.zoomcar.service.VehicleAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for vehicle availability operations.
 * Optimized for high-performance availability queries with sub-5ms response times.
 */
@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class VehicleAvailabilityController {
    
    private final VehicleAvailabilityService availabilityService;
    
    /**
     * Get available vehicles by city and date
     * GET /api/v1/availability/cities/{cityId}?date=2024-08-27
     */
    @GetMapping("/cities/{cityId}")
    public ResponseEntity<List<VehicleAvailabilityResponse>> getAvailableVehiclesByCity(
            @PathVariable UUID cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<VehicleAvailabilityResponse> availability = 
            availabilityService.getAvailableVehiclesByCity(cityId, date);
        
        return ResponseEntity.ok(availability);
    }
    
    /**
     * Get available vehicles by hub and date
     * GET /api/v1/availability/hubs/{hubId}?date=2024-08-27
     */
    @GetMapping("/hubs/{hubId}")
    public ResponseEntity<List<VehicleAvailabilityResponse>> getAvailableVehiclesByHub(
            @PathVariable UUID hubId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<VehicleAvailabilityResponse> availability = 
            availabilityService.getAvailableVehiclesByHub(hubId, date);
        
        return ResponseEntity.ok(availability);
    }
    
    /**
     * Get available vehicles by city, category and date
     * GET /api/v1/availability/cities/{cityId}/categories/{categoryId}?date=2024-08-27
     */
    @GetMapping("/cities/{cityId}/categories/{categoryId}")
    public ResponseEntity<VehicleAvailabilityResponse> getAvailableVehiclesByCategory(
            @PathVariable UUID cityId,
            @PathVariable UUID categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        VehicleAvailabilityResponse availability = 
            availabilityService.getAvailableVehiclesByCategory(cityId, categoryId, date);
        
        return ResponseEntity.ok(availability);
    }
    
    /**
     * Get available vehicles by vehicle type
     * GET /api/v1/availability/cities/{cityId}/types/{vehicleType}?date=2024-08-27
     */
    @GetMapping("/cities/{cityId}/types/{vehicleType}")
    public ResponseEntity<List<VehicleAvailabilityResponse>> getAvailableVehiclesByType(
            @PathVariable UUID cityId,
            @PathVariable VehicleType vehicleType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<VehicleAvailabilityResponse> availability = 
            availabilityService.getAvailableVehiclesByType(cityId, vehicleType, date);
        
        return ResponseEntity.ok(availability);
    }
    
    /**
     * Get available vehicles within time range
     * GET /api/v1/availability/cities/{cityId}/timerange?startTime=2024-08-27T10:00:00&endTime=2024-08-27T18:00:00
     */
    @GetMapping("/cities/{cityId}/timerange")
    public ResponseEntity<List<VehicleAvailabilityResponse>> getAvailableVehiclesByTimeRange(
            @PathVariable UUID cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<VehicleAvailabilityResponse> availability = 
            availabilityService.getAvailableVehiclesByTimeRange(cityId, startTime, endTime);
        
        return ResponseEntity.ok(availability);
    }
    
    /**
     * Get available vehicles by hub within time range
     * GET /api/v1/availability/hubs/{hubId}/timerange?startTime=2024-08-27T10:00:00&endTime=2024-08-27T18:00:00
     */
    @GetMapping("/hubs/{hubId}/timerange")
    public ResponseEntity<List<VehicleAvailabilityResponse>> getAvailableVehiclesByHubAndTimeRange(
            @PathVariable UUID hubId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<VehicleAvailabilityResponse> availability = 
            availabilityService.getAvailableVehiclesByHubAndTimeRange(hubId, startTime, endTime);
        
        return ResponseEntity.ok(availability);
    }
    
    /**
     * Check if specific vehicle is available
     * GET /api/v1/availability/vehicles/{vehicleId}?startTime=2024-08-27T10:00:00&endTime=2024-08-27T18:00:00
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<Boolean> isVehicleAvailable(
            @PathVariable UUID vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        boolean isAvailable = availabilityService.isVehicleAvailable(vehicleId, startTime, endTime);
        
        return ResponseEntity.ok(isAvailable);
    }
    
    /**
     * Get total available vehicle count for city
     * GET /api/v1/availability/cities/{cityId}/count?date=2024-08-27
     */
    @GetMapping("/cities/{cityId}/count")
    public ResponseEntity<Long> getTotalAvailableVehicleCount(
            @PathVariable UUID cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        Long count = availabilityService.getTotalAvailableVehicleCount(cityId, date);
        
        return ResponseEntity.ok(count);
    }
    
    /**
     * Get available vehicles sorted by price
     * GET /api/v1/availability/cities/{cityId}/sorted?date=2024-08-27&ascending=true
     */
    @GetMapping("/cities/{cityId}/sorted")
    public ResponseEntity<List<VehicleAvailabilityResponse>> getAvailableVehiclesSortedByPrice(
            @PathVariable UUID cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "true") boolean ascending) {
        
        List<VehicleAvailabilityResponse> availability = 
            availabilityService.getAvailableVehiclesSortedByPrice(cityId, date, ascending);
        
        return ResponseEntity.ok(availability);
    }
    
    /**
     * Search available vehicles by features
     * GET /api/v1/availability/cities/{cityId}/search?date=2024-08-27&features=GPS,AC,Bluetooth
     */
    @GetMapping("/cities/{cityId}/search")
    public ResponseEntity<List<VehicleAvailabilityResponse>> searchAvailableVehiclesByFeatures(
            @PathVariable UUID cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam List<String> features) {
        
        List<VehicleAvailabilityResponse> availability = 
            availabilityService.searchAvailableVehiclesByFeatures(cityId, date, features);
        
        return ResponseEntity.ok(availability);
    }
    
    /**
     * Get availability summary for all cities
     * GET /api/v1/availability/summary?date=2024-08-27
     */
    @GetMapping("/summary")
    public ResponseEntity<List<VehicleAvailabilityResponse>> getAvailabilitySummaryByCity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<VehicleAvailabilityResponse> summary = 
            availabilityService.getAvailabilitySummaryByCity(date);
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get availability summary for specific city (hub-wise)
     * GET /api/v1/availability/cities/{cityId}/summary?date=2024-08-27
     */
    @GetMapping("/cities/{cityId}/summary")
    public ResponseEntity<List<VehicleAvailabilityResponse>> getAvailabilitySummaryByHub(
            @PathVariable UUID cityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<VehicleAvailabilityResponse> summary = 
            availabilityService.getAvailabilitySummaryByHub(cityId, date);
        
        return ResponseEntity.ok(summary);
    }
}
