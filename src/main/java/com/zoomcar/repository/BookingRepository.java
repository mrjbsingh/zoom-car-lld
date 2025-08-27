package com.zoomcar.repository;

import com.zoomcar.entity.Booking;
import com.zoomcar.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Booking entity with optimized queries for high-concurrency scenarios.
 * Uses UUID-only relationships to avoid JOINs and improve performance.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Find active bookings for a user (CONFIRMED, ONGOING, COMPLETED)
     * Uses denormalized user_name field to avoid JOIN with User table
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
           "AND b.status IN ('CONFIRMED', 'ONGOING', 'COMPLETED') " +
           "ORDER BY b.startTime DESC")
    Page<Booking> findActiveBookingsByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find bookings by vehicle ID within a time range
     * Critical for availability checking - uses indexes on vehicle_id and time columns
     */
    @Query("SELECT b FROM Booking b WHERE b.vehicleId = :vehicleId " +
           "AND b.status IN ('CONFIRMED', 'ONGOING') " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findConflictingBookings(@Param("vehicleId") UUID vehicleId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * Find all confirmed bookings for multiple vehicles in a time range
     * Used for bulk availability checking
     */
    @Query("SELECT b FROM Booking b WHERE b.vehicleId IN :vehicleIds " +
           "AND b.status IN ('CONFIRMED', 'ONGOING') " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    List<Booking> findConflictingBookingsForVehicles(@Param("vehicleIds") List<UUID> vehicleIds,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * Find bookings by hub and time range for analytics
     * Uses denormalized hub fields to avoid JOINs
     */
    @Query("SELECT b FROM Booking b WHERE b.pickupHubId = :hubId " +
           "AND b.startTime >= :startTime AND b.startTime <= :endTime " +
           "ORDER BY b.startTime DESC")
    List<Booking> findBookingsByHubAndTimeRange(@Param("hubId") UUID hubId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * Find bookings by city and time range for analytics
     * Uses denormalized city fields to avoid JOINs
     */
    @Query("SELECT b FROM Booking b WHERE b.pickupCityId = :cityId " +
           "AND b.startTime >= :startTime AND b.startTime <= :endTime " +
           "ORDER BY b.startTime DESC")
    List<Booking> findBookingsByCityAndTimeRange(@Param("cityId") UUID cityId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * Update booking status - used for state transitions
     * Includes optimistic locking check
     */
    @Modifying
    @Query("UPDATE Booking b SET b.status = :status, b.updatedAt = :updatedAt " +
           "WHERE b.bookingId = :bookingId")
    int updateBookingStatus(@Param("bookingId") UUID bookingId,
                           @Param("status") BookingStatus status,
                           @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Find bookings that need to be marked as ONGOING
     * Background job query - finds CONFIRMED bookings where start time has passed
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND b.startTime <= :currentTime " +
           "ORDER BY b.startTime ASC")
    List<Booking> findBookingsToMarkAsOngoing(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find bookings that need to be marked as COMPLETED
     * Background job query - finds ONGOING bookings where end time has passed
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'ONGOING' " +
           "AND b.endTime <= :currentTime " +
           "ORDER BY b.endTime ASC")
    List<Booking> findBookingsToMarkAsCompleted(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find expired PENDING bookings for cleanup
     * Background job query - finds bookings pending payment beyond timeout
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' " +
           "AND b.createdAt <= :expiryTime")
    List<Booking> findExpiredPendingBookings(@Param("expiryTime") LocalDateTime expiryTime);

    /**
     * Count active bookings for a vehicle in a time range
     * Fast count query for availability checking
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vehicleId = :vehicleId " +
           "AND b.status IN ('CONFIRMED', 'ONGOING') " +
           "AND ((b.startTime <= :endTime AND b.endTime >= :startTime))")
    long countConflictingBookings(@Param("vehicleId") UUID vehicleId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * Find user's booking history with pagination
     * Uses denormalized fields for fast display without JOINs
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
           "ORDER BY b.createdAt DESC")
    Page<Booking> findBookingHistoryByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find bookings by booking reference for customer support
     */
    Optional<Booking> findByBookingReference(String bookingReference);

    /**
     * Revenue analytics query - sum of total amounts by city and date range
     */
    @Query("SELECT b.pickupCityId, b.pickupCityName, SUM(b.totalAmount) " +
           "FROM Booking b WHERE b.status = 'COMPLETED' " +
           "AND b.startTime >= :startDate AND b.startTime <= :endDate " +
           "GROUP BY b.pickupCityId, b.pickupCityName")
    List<Object[]> getRevenueByCity(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Vehicle utilization analytics - count bookings per vehicle
     */
    @Query("SELECT b.vehicleId, b.vehicleRegistrationNumber, COUNT(b) " +
           "FROM Booking b WHERE b.status IN ('CONFIRMED', 'ONGOING', 'COMPLETED') " +
           "AND b.startTime >= :startDate AND b.startTime <= :endDate " +
           "GROUP BY b.vehicleId, b.vehicleRegistrationNumber " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> getVehicleUtilization(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}
