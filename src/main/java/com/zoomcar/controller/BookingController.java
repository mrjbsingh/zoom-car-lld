package com.zoomcar.controller;

import com.zoomcar.dto.BookingRequest;
import com.zoomcar.dto.BookingResponse;
import com.zoomcar.enums.BookingStatus;
import com.zoomcar.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for booking operations following RESTful principles.
 * Implements proper HTTP status codes and error handling.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class BookingController {
    
    private final BookingService bookingService;
    
    /**
     * Create a new booking
     * POST /api/v1/bookings
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("Creating booking for user {} and vehicle {}", request.getUserId(), request.getVehicleId());
        
        BookingResponse response = bookingService.createBooking(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get booking by ID
     * GET /api/v1/bookings/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID bookingId) {
        log.debug("Fetching booking with ID: {}", bookingId);
        
        BookingResponse response = bookingService.getBookingById(bookingId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get booking by reference number
     * GET /api/v1/bookings/reference/{bookingReference}
     */
    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<BookingResponse> getBookingByReference(@PathVariable String bookingReference) {
        log.debug("Fetching booking with reference: {}", bookingReference);
        
        BookingResponse response = bookingService.getBookingByReference(bookingReference);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user's booking history
     * GET /api/v1/bookings/users/{userId}/history
     */
    @GetMapping("/users/{userId}/history")
    public ResponseEntity<Page<BookingResponse>> getUserBookingHistory(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.debug("Fetching booking history for user: {}", userId);
        
        Page<BookingResponse> bookings = bookingService.getUserBookingHistory(userId, pageable);
        
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Get user's active bookings
     * GET /api/v1/bookings/users/{userId}/active
     */
    @GetMapping("/users/{userId}/active")
    public ResponseEntity<Page<BookingResponse>> getUserActiveBookings(
            @PathVariable UUID userId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        log.debug("Fetching active bookings for user: {}", userId);
        
        Page<BookingResponse> bookings = bookingService.getUserActiveBookings(userId, pageable);
        
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Cancel a booking
     * PUT /api/v1/bookings/{bookingId}/cancel
     */
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable UUID bookingId,
            @RequestParam UUID userId) {
        
        log.info("Cancelling booking {} by user {}", bookingId, userId);
        
        BookingResponse response = bookingService.cancelBooking(bookingId, userId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update booking status (admin operation)
     * PUT /api/v1/bookings/{bookingId}/status
     */
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable UUID bookingId,
            @RequestParam BookingStatus status) {
        
        log.info("Updating booking {} status to {}", bookingId, status);
        
        BookingResponse response = bookingService.updateBookingStatus(bookingId, status);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get bookings by hub and time range (analytics endpoint)
     * GET /api/v1/bookings/hubs/{hubId}
     */
    @GetMapping("/hubs/{hubId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByHub(
            @PathVariable UUID hubId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        
        log.debug("Fetching bookings for hub {} between {} and {}", hubId, startTime, endTime);
        
        List<BookingResponse> bookings = bookingService.getBookingsByHubAndTimeRange(hubId, startTime, endTime);
        
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Get bookings by city and time range (analytics endpoint)
     * GET /api/v1/bookings/cities/{cityId}
     */
    @GetMapping("/cities/{cityId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByCity(
            @PathVariable UUID cityId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        
        log.debug("Fetching bookings for city {} between {} and {}", cityId, startTime, endTime);
        
        List<BookingResponse> bookings = bookingService.getBookingsByCityAndTimeRange(cityId, startTime, endTime);
        
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Check if vehicle has conflicting bookings
     * GET /api/v1/bookings/vehicles/{vehicleId}/conflicts
     */
    @GetMapping("/vehicles/{vehicleId}/conflicts")
    public ResponseEntity<Boolean> checkVehicleConflicts(
            @PathVariable UUID vehicleId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        
        log.debug("Checking conflicts for vehicle {} between {} and {}", vehicleId, startTime, endTime);
        
        boolean hasConflicts = bookingService.hasConflictingBookings(vehicleId, startTime, endTime);
        
        return ResponseEntity.ok(hasConflicts);
    }
}
