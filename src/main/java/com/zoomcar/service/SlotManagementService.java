package com.zoomcar.service;

import com.zoomcar.entity.VehicleAvailabilitySlot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing vehicle availability slots with optimistic locking.
 * Handles slot locking, booking confirmation, and aggregated data updates.
 */
public interface SlotManagementService {
    
    /**
     * Lock slots for booking with optimistic locking
     * 
     * @param vehicleId Vehicle identifier
     * @param startTime Booking start time
     * @param endTime Booking end time
     * @return List of locked slots
     * @throws OptimisticLockingException if slots are already booked
     */
    List<VehicleAvailabilitySlot> lockSlotsForBooking(UUID vehicleId, 
                                                     LocalDateTime startTime, 
                                                     LocalDateTime endTime);
    
    /**
     * Confirm slot booking after successful booking creation
     * 
     * @param slots List of slots to confirm
     * @param bookingId Booking identifier
     * @throws OptimisticLockingException if version mismatch
     */
    void confirmSlotBooking(List<VehicleAvailabilitySlot> slots, UUID bookingId);
    
    /**
     * Release slots when booking is cancelled
     * 
     * @param bookingId Booking identifier
     */
    void releaseBookingSlots(UUID bookingId);
    
    /**
     * Update aggregated availability data after slot changes
     * 
     * @param vehicleId Vehicle identifier
     * @param startTime Time range start
     * @param endTime Time range end
     */
    void updateAggregatedAvailability(UUID vehicleId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Create availability slots for a vehicle for future dates
     * Background job method
     * 
     * @param vehicleId Vehicle identifier
     * @param startDate Start date for slot creation
     * @param endDate End date for slot creation
     */
    void createSlotsForVehicle(UUID vehicleId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Sync aggregated availability data from individual slots
     * Background job method
     */
    void syncAggregatedAvailabilityData();
    
    /**
     * Clean up old slots to prevent table bloat
     * Background job method
     * 
     * @param cutoffDate Date before which to delete slots
     * @return Number of slots deleted
     */
    int cleanupOldSlots(LocalDateTime cutoffDate);
}
