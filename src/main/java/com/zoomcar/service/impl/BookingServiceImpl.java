package com.zoomcar.service.impl;

import com.zoomcar.dto.BookingRequest;
import com.zoomcar.dto.BookingResponse;
import com.zoomcar.entity.Booking;
import com.zoomcar.entity.VehicleAvailabilitySlot;
import com.zoomcar.enums.BookingStatus;
import com.zoomcar.exception.*;
import com.zoomcar.repository.BookingRepository;
import com.zoomcar.repository.VehicleAvailabilitySlotRepository;
import com.zoomcar.repository.VehicleSlotAvailabilityRepository;
import com.zoomcar.service.BookingService;
import com.zoomcar.service.PricingService;
import com.zoomcar.service.SlotManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of BookingService following SOLID principles.
 * Uses Strategy pattern for pricing and Template Method pattern for booking flow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    
    private final BookingRepository bookingRepository;
    private final VehicleAvailabilitySlotRepository slotRepository;
    private final VehicleSlotAvailabilityRepository aggregatedSlotRepository;
    private final PricingService pricingService;
    private final SlotManagementService slotManagementService;
    
    @Value("${zoomcar.booking.max-retry-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${zoomcar.booking.retry-delay-ms:100}")
    private long retryDelayMs;
    
    /**
     * Create booking with optimistic locking and retry mechanism
     * Template Method Pattern: defines the algorithm structure
     */
    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for user {} and vehicle {}", request.getUserId(), request.getVehicleId());
        
        // Validate request
        validateBookingRequest(request);
        
        // Attempt booking with retry mechanism
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                return attemptBookingCreation(request, attempt);
            } catch (OptimisticLockingException e) {
                log.warn("Optimistic locking conflict on attempt {} for vehicle {}", 
                        attempt, request.getVehicleId());
                
                if (attempt == maxRetryAttempts) {
                    throw new BookingConflictException(
                        "Unable to complete booking after " + maxRetryAttempts + " attempts. Please try again.");
                }
                
                // Exponential backoff
                try {
                    Thread.sleep(retryDelayMs * (1L << (attempt - 1)));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BookingConflictException("Booking interrupted", ie);
                }
            }
        }
        
        throw new BookingConflictException("Max retry attempts exceeded");
    }
    
    /**
     * Single booking attempt with optimistic locking
     */
    private BookingResponse attemptBookingCreation(BookingRequest request, int attempt) {
        // 1. Check vehicle availability and lock slots
        List<VehicleAvailabilitySlot> availableSlots = slotManagementService
            .lockSlotsForBooking(request.getVehicleId(), request.getStartTime(), request.getEndTime());
        
        if (availableSlots.isEmpty()) {
            throw new BookingConflictException("Vehicle not available for requested time slots");
        }
        
        // 2. Calculate pricing using Strategy pattern
        var pricingResult = pricingService.calculateBookingPrice(request);
        
        // 3. Create booking entity
        Booking booking = createBookingEntity(request, pricingResult);
        
        // 4. Save booking
        booking = bookingRepository.save(booking);
        
        // 5. Update slots with optimistic locking
        slotManagementService.confirmSlotBooking(availableSlots, booking.getId());
        
        // 6. Update denormalized availability data
        slotManagementService.updateAggregatedAvailability(
            request.getVehicleId(), request.getStartTime(), request.getEndTime());
        
        // 7. Clear caches
        clearAvailabilityCaches(request.getVehicleId());
        
        log.info("Booking created successfully: {} for user {}", booking.getId(), request.getUserId());
        
        return mapToBookingResponse(booking);
    }
    
    @Override
    @Cacheable(value = "bookings", key = "#bookingId")
    public BookingResponse getBookingById(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        return mapToBookingResponse(booking);
    }
    
    @Override
    @Cacheable(value = "bookings", key = "#bookingReference")
    public BookingResponse getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
            .orElseThrow(() -> new BookingNotFoundException(bookingReference));
        
        return mapToBookingResponse(booking);
    }
    
    @Override
    public Page<BookingResponse> getUserBookingHistory(UUID userId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findBookingHistoryByUserId(userId, pageable);
        return bookings.map(this::mapToBookingResponse);
    }
    
    @Override
    public Page<BookingResponse> getUserActiveBookings(UUID userId, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findActiveBookingsByUserId(userId, pageable);
        return bookings.map(this::mapToBookingResponse);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "bookings", key = "#bookingId")
    public BookingResponse cancelBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        // Validate cancellation
        validateBookingCancellation(booking, userId);
        
        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);
        
        // Release slots
        slotManagementService.releaseBookingSlots(bookingId);
        
        // Update aggregated availability
        slotManagementService.updateAggregatedAvailability(
            booking.getVehicleId(), booking.getStartTime(), booking.getEndTime());
        
        // Clear caches
        clearAvailabilityCaches(booking.getVehicleId());
        
        log.info("Booking cancelled: {} by user {}", bookingId, userId);
        
        return mapToBookingResponse(booking);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "bookings", key = "#bookingId")
    public BookingResponse updateBookingStatus(UUID bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        // Validate status transition
        validateStatusTransition(booking.getStatus(), newStatus);
        
        // Update status
        int updated = bookingRepository.updateBookingStatus(
            bookingId, newStatus, LocalDateTime.now(), booking.getVersionNumber());
        
        if (updated == 0) {
            throw new OptimisticLockingException("Booking", bookingId.toString(), 
                booking.getVersionNumber(), null);
        }
        
        // Refresh entity
        booking = bookingRepository.findById(bookingId).orElseThrow();
        
        log.info("Booking status updated: {} -> {}", bookingId, newStatus);
        
        return mapToBookingResponse(booking);
    }
    
    @Override
    public List<BookingResponse> getBookingsByHubAndTimeRange(UUID hubId, 
                                                             LocalDateTime startTime, 
                                                             LocalDateTime endTime) {
        List<Booking> bookings = bookingRepository.findBookingsByHubAndTimeRange(hubId, startTime, endTime);
        return bookings.stream()
            .map(this::mapToBookingResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingResponse> getBookingsByCityAndTimeRange(UUID cityId, 
                                                              LocalDateTime startTime, 
                                                              LocalDateTime endTime) {
        List<Booking> bookings = bookingRepository.findBookingsByCityAndTimeRange(cityId, startTime, endTime);
        return bookings.stream()
            .map(this::mapToBookingResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasConflictingBookings(UUID vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        long conflictCount = bookingRepository.countConflictingBookings(vehicleId, startTime, endTime);
        return conflictCount > 0;
    }
    
    @Override
    public List<Booking> getBookingsNeedingStatusUpdate() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> ongoingBookings = bookingRepository.findBookingsToMarkAsOngoing(now);
        List<Booking> completedBookings = bookingRepository.findBookingsToMarkAsCompleted(now);
        
        ongoingBookings.addAll(completedBookings);
        return ongoingBookings;
    }
    
    @Override
    public List<Booking> getExpiredPendingBookings(LocalDateTime expiryTime) {
        return bookingRepository.findExpiredPendingBookings(expiryTime);
    }
    
    // Private helper methods
    
    private void validateBookingRequest(BookingRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
        
        if (request.getStartTime().isBefore(LocalDateTime.now().plusMinutes(30))) {
            throw new IllegalArgumentException("Booking must be at least 30 minutes in advance");
        }
    }
    
    private Booking createBookingEntity(BookingRequest request, PricingService.PricingResult pricingResult) {
        return Booking.builder()
            .id(UUID.randomUUID())
            .bookingReference(generateBookingReference())
            .userId(request.getUserId())
            .vehicleId(request.getVehicleId())
            .pickupHubId(request.getPickupHubId())
            .dropHubId(request.getDropHubId())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .bookingType(request.getBookingType())
            .status(BookingStatus.PENDING)
            .baseAmount(pricingResult.getBaseAmount())
            .taxAmount(pricingResult.getTaxAmount())
            .totalAmount(pricingResult.getTotalAmount())
            .specialRequests(request.getSpecialRequests())
            .promoCode(request.getPromoCode())
            .discountAmount(pricingResult.getDiscountAmount())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .versionNumber(1L)
            .build();
    }
    
    private void validateBookingCancellation(Booking booking, UUID userId) {
        if (!booking.getUserId().equals(userId)) {
            throw new BookingCancellationNotAllowedException(
                booking.getId(), booking.getStatus(), "User not authorized to cancel this booking");
        }
        
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingCancellationNotAllowedException(
                booking.getId(), booking.getStatus(), "Booking is already cancelled");
        }
        
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BookingCancellationNotAllowedException(
                booking.getId(), booking.getStatus(), "Cannot cancel completed booking");
        }
        
        // Check cancellation time limits
        if (booking.getStartTime().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BookingCancellationNotAllowedException(
                booking.getId(), booking.getStatus(), "Cannot cancel booking less than 2 hours before start time");
        }
    }
    
    private void validateStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        // Define valid transitions
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == BookingStatus.CONFIRMED || newStatus == BookingStatus.CANCELLED;
            case CONFIRMED -> newStatus == BookingStatus.ONGOING || newStatus == BookingStatus.CANCELLED;
            case ONGOING -> newStatus == BookingStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false; // Terminal states
        };
        
        if (!isValidTransition) {
            throw new InvalidStatusTransitionException(null, currentStatus, newStatus);
        }
    }
    
    private String generateBookingReference() {
        return "ZC" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private void clearAvailabilityCaches(UUID vehicleId) {
        // Implementation would clear Redis caches for availability data
        log.debug("Clearing availability caches for vehicle {}", vehicleId);
    }
    
    private BookingResponse mapToBookingResponse(Booking booking) {
        return BookingResponse.builder()
            .bookingId(booking.getId())
            .bookingReference(booking.getBookingReference())
            .userId(booking.getUserId())
            .userName(booking.getUserName())
            .vehicleId(booking.getVehicleId())
            .vehicleRegistrationNumber(booking.getVehicleRegistrationNumber())
            .vehicleModel(booking.getVehicleModel())
            .vehicleBrand(booking.getVehicleBrand())
            .categoryName(booking.getCategoryName())
            .pickupHubId(booking.getPickupHubId())
            .pickupHubName(booking.getPickupHubName())
            .pickupHubAddress(booking.getPickupHubAddress())
            .dropHubId(booking.getDropHubId())
            .dropHubName(booking.getDropHubName())
            .dropHubAddress(booking.getDropHubAddress())
            .startTime(booking.getStartTime())
            .endTime(booking.getEndTime())
            .bookingType(booking.getBookingType())
            .status(booking.getStatus())
            .baseAmount(booking.getBaseAmount())
            .taxAmount(booking.getTaxAmount())
            .totalAmount(booking.getTotalAmount())
            .specialRequests(booking.getSpecialRequests())
            .promoCode(booking.getPromoCode())
            .discountAmount(booking.getDiscountAmount())
            .createdAt(booking.getCreatedAt())
            .updatedAt(booking.getUpdatedAt())
            .build();
    }
}
