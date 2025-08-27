package com.zoomcar.service;

import com.zoomcar.dto.BookingRequest;
import com.zoomcar.dto.BookingResponse;
import com.zoomcar.entity.Booking;
import com.zoomcar.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for booking operations following Single Responsibility Principle.
 * Handles all booking-related business logic with optimistic locking for concurrency control.
 */
public interface BookingService {
    
    /**
     * Create a new booking with optimistic locking to prevent double bookings
     * 
     * @param request Booking request details
     * @return BookingResponse with booking details or conflict information
     * @throws BookingConflictException if slots are no longer available
     * @throws OptimisticLockingException if concurrent modification detected
     */
    BookingResponse createBooking(BookingRequest request);
    
    /**
     * Get booking by ID with full details
     * 
     * @param bookingId Booking identifier
     * @return BookingResponse with complete booking details
     * @throws BookingNotFoundException if booking not found
     */
    BookingResponse getBookingById(UUID bookingId);
    
    /**
     * Get booking by reference number
     * 
     * @param bookingReference Booking reference number
     * @return BookingResponse with complete booking details
     * @throws BookingNotFoundException if booking not found
     */
    BookingResponse getBookingByReference(String bookingReference);
    
    /**
     * Get user's booking history with pagination
     * 
     * @param userId User identifier
     * @param pageable Pagination parameters
     * @return Page of BookingResponse objects
     */
    Page<BookingResponse> getUserBookingHistory(UUID userId, Pageable pageable);
    
    /**
     * Get user's active bookings (CONFIRMED, ONGOING, COMPLETED)
     * 
     * @param userId User identifier
     * @param pageable Pagination parameters
     * @return Page of active BookingResponse objects
     */
    Page<BookingResponse> getUserActiveBookings(UUID userId, Pageable pageable);
    
    /**
     * Cancel a booking if cancellation is allowed
     * 
     * @param bookingId Booking identifier
     * @param userId User identifier (for authorization)
     * @return Updated BookingResponse
     * @throws BookingNotFoundException if booking not found
     * @throws BookingCancellationNotAllowedException if cancellation not allowed
     */
    BookingResponse cancelBooking(UUID bookingId, UUID userId);
    
    /**
     * Update booking status (used by background jobs and admin operations)
     * 
     * @param bookingId Booking identifier
     * @param newStatus New booking status
     * @return Updated BookingResponse
     * @throws BookingNotFoundException if booking not found
     * @throws InvalidStatusTransitionException if status transition not allowed
     */
    BookingResponse updateBookingStatus(UUID bookingId, BookingStatus newStatus);
    
    /**
     * Get bookings by hub and time range for analytics
     * 
     * @param hubId Hub identifier
     * @param startTime Start time range
     * @param endTime End time range
     * @return List of BookingResponse objects
     */
    List<BookingResponse> getBookingsByHubAndTimeRange(UUID hubId, 
                                                      LocalDateTime startTime, 
                                                      LocalDateTime endTime);
    
    /**
     * Get bookings by city and time range for analytics
     * 
     * @param cityId City identifier
     * @param startTime Start time range
     * @param endTime End time range
     * @return List of BookingResponse objects
     */
    List<BookingResponse> getBookingsByCityAndTimeRange(UUID cityId, 
                                                       LocalDateTime startTime, 
                                                       LocalDateTime endTime);
    
    /**
     * Check if vehicle has conflicting bookings for given time range
     * 
     * @param vehicleId Vehicle identifier
     * @param startTime Start time
     * @param endTime End time
     * @return true if conflicts exist, false otherwise
     */
    boolean hasConflictingBookings(UUID vehicleId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get bookings that need status updates (background job support)
     * 
     * @return List of bookings needing status updates
     */
    List<Booking> getBookingsNeedingStatusUpdate();
    
    /**
     * Get expired pending bookings for cleanup
     * 
     * @param expiryTime Expiry threshold time
     * @return List of expired bookings
     */
    List<Booking> getExpiredPendingBookings(LocalDateTime expiryTime);
}
