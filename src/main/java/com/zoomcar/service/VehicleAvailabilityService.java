package com.zoomcar.service;

import com.zoomcar.dto.VehicleAvailabilityResponse;
import com.zoomcar.enums.VehicleType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for vehicle availability operations following Single Responsibility Principle.
 * Handles fast availability queries using denormalized data for sub-5ms response times.
 */
public interface VehicleAvailabilityService {
    
    /**
     * Get available vehicles by city and date (primary availability query)
     * Optimized for sub-5ms response using denormalized VehicleSlotAvailability table
     * 
     * @param cityId City identifier
     * @param date Date for availability check
     * @return List of available vehicle categories with counts
     */
    List<VehicleAvailabilityResponse> getAvailableVehiclesByCity(UUID cityId, LocalDate date);
    
    /**
     * Get available vehicles by hub and date
     * Hub-specific availability for location-based searches
     * 
     * @param hubId Hub identifier
     * @param date Date for availability check
     * @return List of available vehicle categories with counts
     */
    List<VehicleAvailabilityResponse> getAvailableVehiclesByHub(UUID hubId, LocalDate date);
    
    /**
     * Get available vehicles by city, category and date
     * Category-specific availability for filtered searches
     * 
     * @param cityId City identifier
     * @param categoryId Category identifier
     * @param date Date for availability check
     * @return VehicleAvailabilityResponse with detailed slot information
     */
    VehicleAvailabilityResponse getAvailableVehiclesByCategory(UUID cityId, 
                                                              UUID categoryId, 
                                                              LocalDate date);
    
    /**
     * Get available vehicles by vehicle type (CAR vs BIKE)
     * Type-specific availability queries
     * 
     * @param cityId City identifier
     * @param vehicleType Vehicle type (CAR/BIKE)
     * @param date Date for availability check
     * @return List of available vehicles of specified type
     */
    List<VehicleAvailabilityResponse> getAvailableVehiclesByType(UUID cityId, 
                                                                VehicleType vehicleType, 
                                                                LocalDate date);
    
    /**
     * Get available vehicles within time range
     * Time-specific availability for precise booking requirements
     * 
     * @param cityId City identifier
     * @param startTime Start time for availability check
     * @param endTime End time for availability check
     * @return List of available vehicles for the time range
     */
    List<VehicleAvailabilityResponse> getAvailableVehiclesByTimeRange(UUID cityId, 
                                                                     LocalDateTime startTime, 
                                                                     LocalDateTime endTime);
    
    /**
     * Get available vehicles by hub within time range
     * Hub and time specific availability
     * 
     * @param hubId Hub identifier
     * @param startTime Start time for availability check
     * @param endTime End time for availability check
     * @return List of available vehicles for the hub and time range
     */
    List<VehicleAvailabilityResponse> getAvailableVehiclesByHubAndTimeRange(UUID hubId, 
                                                                           LocalDateTime startTime, 
                                                                           LocalDateTime endTime);
    
    /**
     * Check if specific vehicle is available for given time range
     * Individual vehicle availability check for booking validation
     * 
     * @param vehicleId Vehicle identifier
     * @param startTime Start time
     * @param endTime End time
     * @return true if vehicle is available, false otherwise
     */
    boolean isVehicleAvailable(UUID vehicleId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get total available vehicle count for city and date
     * Quick count query for dashboard and analytics
     * 
     * @param cityId City identifier
     * @param date Date for count
     * @return Total count of available vehicles
     */
    Long getTotalAvailableVehicleCount(UUID cityId, LocalDate date);
    
    /**
     * Get available vehicles sorted by price
     * Price-optimized search for budget-conscious users
     * 
     * @param cityId City identifier
     * @param date Date for availability check
     * @param ascending true for low to high, false for high to low
     * @return List of available vehicles sorted by price
     */
    List<VehicleAvailabilityResponse> getAvailableVehiclesSortedByPrice(UUID cityId, 
                                                                       LocalDate date, 
                                                                       boolean ascending);
    
    /**
     * Search available vehicles by features
     * Feature-based availability search (e.g., GPS, AC, etc.)
     * 
     * @param cityId City identifier
     * @param date Date for availability check
     * @param requiredFeatures List of required features
     * @return List of available vehicles with required features
     */
    List<VehicleAvailabilityResponse> searchAvailableVehiclesByFeatures(UUID cityId, 
                                                                        LocalDate date, 
                                                                        List<String> requiredFeatures);
    
    /**
     * Get availability summary for dashboard
     * City-wise availability summary for operations dashboard
     * 
     * @param date Date for summary
     * @return List of city-wise availability summaries
     */
    List<VehicleAvailabilityResponse> getAvailabilitySummaryByCity(LocalDate date);
    
    /**
     * Get availability summary for specific city
     * Hub-wise availability summary for city operations
     * 
     * @param cityId City identifier
     * @param date Date for summary
     * @return List of hub-wise availability summaries
     */
    List<VehicleAvailabilityResponse> getAvailabilitySummaryByHub(UUID cityId, LocalDate date);
}
