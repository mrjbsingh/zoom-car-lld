package com.zoomcar.repository;

import com.zoomcar.entity.VehicleAvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for VehicleAvailabilitySlot entity.
 * Handles individual vehicle slot availability with optimistic locking for concurrency control.
 * This is the primary table for booking operations to prevent double bookings.
 */
@Repository
public interface VehicleAvailabilitySlotRepository extends JpaRepository<VehicleAvailabilitySlot, UUID> {

    /**
     * Find available slot for a specific vehicle, date and hour
     * Critical query for booking - uses optimistic locking
     */
    @Query("SELECT vas FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.vehicleId = :vehicleId " +
           "AND vas.date = :date " +
           "AND vas.hourSlot = :hourSlot " +
           "AND vas.isAvailable = true")
    Optional<VehicleAvailabilitySlot> findAvailableSlot(@Param("vehicleId") UUID vehicleId,
                                                       @Param("date") LocalDate date,
                                                       @Param("hourSlot") Integer hourSlot);

    /**
     * Find all available slots for a vehicle within a date and hour range
     * Used for availability checking and slot management
     */
    @Query("SELECT vas FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.vehicleId = :vehicleId " +
           "AND ((vas.date = :startDate AND vas.hourSlot >= :startHour) OR vas.date > :startDate) " +
           "AND ((vas.date = :endDate AND vas.hourSlot <= :endHour) OR vas.date < :endDate) " +
           "AND vas.isAvailable = true " +
           "ORDER BY vas.date ASC, vas.hourSlot ASC")
    List<VehicleAvailabilitySlot> findAvailableSlotsByVehicleAndTimeRange(
            @Param("vehicleId") UUID vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("startHour") Integer startHour,
            @Param("endDate") LocalDate endDate,
            @Param("endHour") Integer endHour);

    /**
     * Find all slots for a vehicle within a date and hour range (available and booked)
     * Used for slot management and analytics
     */
    @Query("SELECT vas FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.vehicleId = :vehicleId " +
           "AND ((vas.date = :startDate AND vas.hourSlot >= :startHour) OR vas.date > :startDate) " +
           "AND ((vas.date = :endDate AND vas.hourSlot <= :endHour) OR vas.date < :endDate) " +
           "ORDER BY vas.date ASC, vas.hourSlot ASC")
    List<VehicleAvailabilitySlot> findSlotsByVehicleAndTimeRange(
            @Param("vehicleId") UUID vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("startHour") Integer startHour,
            @Param("endDate") LocalDate endDate,
            @Param("endHour") Integer endHour);

    /**
     * Book a slot with optimistic locking
     * Returns 1 if successful, 0 if version mismatch (concurrent booking)
     */
    @Modifying
    @Query("UPDATE VehicleAvailabilitySlot vas " +
           "SET vas.isAvailable = false, " +
           "    vas.bookingId = :bookingId, " +
           "    vas.versionNumber = vas.versionNumber + 1 " +
           "WHERE vas.slotId = :slotId " +
           "AND vas.versionNumber = :expectedVersion " +
           "AND vas.isAvailable = true")
    int bookSlotWithOptimisticLock(@Param("slotId") UUID slotId,
                                  @Param("bookingId") UUID bookingId,
                                  @Param("bookedAt") LocalDateTime bookedAt,
                                  @Param("expectedVersion") Long expectedVersion);

    /**
     * Confirm slot booking with optimistic locking
     * Returns 1 if successful, 0 if version mismatch (concurrent modification)
     */
    @Modifying
    @Query("UPDATE VehicleAvailabilitySlot vas " +
           "SET vas.bookingId = :bookingId, " +
           "    vas.versionNumber = vas.versionNumber + 1 " +
           "WHERE vas.slotId = :slotId " +
           "AND vas.versionNumber = :expectedVersion")
    int confirmSlotBookingWithOptimisticLock(@Param("slotId") UUID slotId,
                                           @Param("bookingId") UUID bookingId,
                                           @Param("expectedVersion") Long expectedVersion);

    /**
     * Release a slot (cancel booking) with optimistic locking
     * Returns 1 if successful, 0 if version mismatch
     */
    @Modifying
    @Query("UPDATE VehicleAvailabilitySlot vas " +
           "SET vas.isAvailable = true, " +
           "    vas.bookingId = null, " +
           "    vas.versionNumber = vas.versionNumber + 1 " +
           "WHERE vas.slotId = :slotId " +
           "AND vas.versionNumber = :expectedVersion")
    int releaseSlotWithOptimisticLock(@Param("slotId") UUID slotId,
                                     @Param("expectedVersion") Long expectedVersion);

    /**
     * Find slots by booking ID
     * Used for booking cancellation and slot release
     */
    @Query("SELECT vas FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.bookingId = :bookingId")
    List<VehicleAvailabilitySlot> findSlotsByBookingId(@Param("bookingId") UUID bookingId);

    /**
     * Count available slots for a vehicle in a time range
     * Fast count query for availability checking
     */
    @Query("SELECT COUNT(vas) FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.vehicleId = :vehicleId " +
           "AND vas.slotStartTime >= :startTime " +
           "AND vas.slotEndTime <= :endTime " +
           "AND vas.isAvailable = true")
    long countAvailableSlots(@Param("vehicleId") UUID vehicleId,
                            @Param("startTime") LocalDateTime startTime,
                            @Param("endTime") LocalDateTime endTime);

    /**
     * Find available slots for multiple vehicles in a time range
     * Used for bulk availability checking across vehicles
     */
    @Query("SELECT vas FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.vehicleId IN :vehicleIds " +
           "AND vas.slotStartTime >= :startTime " +
           "AND vas.slotEndTime <= :endTime " +
           "AND vas.isAvailable = true " +
           "ORDER BY vas.vehicleId, vas.slotStartTime ASC")
    List<VehicleAvailabilitySlot> findAvailableSlotsForVehicles(
            @Param("vehicleIds") List<UUID> vehicleIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find slots that need to be created for a vehicle
     * Background job query - finds missing slots for future dates
     */
    @Query("SELECT DISTINCT v.id FROM Vehicle v " +
           "WHERE v.id = :vehicleId " +
           "AND NOT EXISTS (" +
           "    SELECT 1 FROM VehicleAvailabilitySlot vas " +
           "    WHERE vas.vehicleId = v.id " +
           "    AND vas.slotStartTime = :slotStartTime" +
           ")")
    List<UUID> findVehiclesNeedingSlotCreation(@Param("vehicleId") UUID vehicleId,
                                              @Param("slotStartTime") LocalDateTime slotStartTime);

    /**
     * Delete old slots (cleanup job)
     * Remove slots older than specified date to prevent table bloat
     */
    @Modifying
    @Query("DELETE FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.slotEndTime < :cutoffDate")
    int deleteOldSlots(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find slots by hub and time range for analytics
     * Uses denormalized hub fields to avoid JOINs
     */
    @Query("SELECT vas FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.hubId = :hubId " +
           "AND vas.slotStartTime >= :startTime " +
           "AND vas.slotEndTime <= :endTime " +
           "ORDER BY vas.slotStartTime ASC")
    List<VehicleAvailabilitySlot> findSlotsByHubAndTimeRange(
            @Param("hubId") UUID hubId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Find slots by city and time range for analytics
     * Uses denormalized city fields to avoid JOINs
     */
    @Query("SELECT vas FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.cityId = :cityId " +
           "AND vas.slotStartTime >= :startTime " +
           "AND vas.slotEndTime <= :endTime " +
           "ORDER BY vas.slotStartTime ASC")
    List<VehicleAvailabilitySlot> findSlotsByCityAndTimeRange(
            @Param("cityId") UUID cityId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Vehicle utilization analytics - calculate booking percentage
     */
    @Query("SELECT vas.vehicleId, " +
           "       COUNT(vas) as totalSlots, " +
           "       SUM(CASE WHEN vas.isAvailable = false THEN 1 ELSE 0 END) as bookedSlots " +
           "FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.slotStartTime >= :startDate " +
           "AND vas.slotEndTime <= :endDate " +
           "GROUP BY vas.vehicleId")
    List<Object[]> getVehicleUtilizationStats(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Hub utilization analytics - calculate booking percentage by hub
     */
    @Query("SELECT vas.hubId, vas.hubName, " +
           "       COUNT(vas) as totalSlots, " +
           "       SUM(CASE WHEN vas.isAvailable = false THEN 1 ELSE 0 END) as bookedSlots " +
           "FROM VehicleAvailabilitySlot vas " +
           "WHERE vas.slotStartTime >= :startDate " +
           "AND vas.slotEndTime <= :endDate " +
           "GROUP BY vas.hubId, vas.hubName " +
           "ORDER BY bookedSlots DESC")
    List<Object[]> getHubUtilizationStats(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}
