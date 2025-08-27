package com.zoomcar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vehicle Availability Slot entity - Individual slot tracking with optimistic locking
 * CRITICAL: This table is used for preventing double bookings through optimistic locking
 * 
 * Key Features:
 * - @Version annotation for optimistic locking (prevents race conditions)
 * - Temporary reservation support for UI race condition prevention
 * - Unique constraint on (vehicle_id, date, hour_slot)
 * - Atomic updates with version checking
 * 
 * Concurrency Algorithm:
 * 1. SELECT FOR UPDATE NOWAIT to lock slots
 * 2. Check version number for optimistic locking
 * 3. Update with version increment (atomic operation)
 * 4. Retry with exponential backoff on conflicts
 */
@Entity
@Table(name = "vehicle_availability_slots", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"vehicle_id", "date", "hour_slot"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slot_id")
    private UUID slotId;

    // Store only vehicle_id for performance (no JOIN)
    @Column(name = "vehicle_id", nullable = false)
    private UUID vehicleId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "hour_slot", nullable = false)
    private Integer hourSlot; // 0-23 (24-hour format)

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "booking_id")
    private UUID bookingId;

    // CRITICAL: Optimistic locking for concurrency control
    @Version
    @Column(name = "version_number")
    @Builder.Default
    private Long versionNumber = 1L;

    // Temporary reservation fields for preventing UI race conditions
    @Column(name = "reserved_until")
    private Instant reservedUntil;

    @Column(name = "reserved_by_session", length = 100)
    private String reservedBySession;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Helper methods for business logic
    public boolean isReserved() {
        return reservedUntil != null && reservedUntil.isAfter(Instant.now());
    }

    public boolean isReservedBySession(String sessionId) {
        return isReserved() && sessionId.equals(reservedBySession);
    }

    public boolean isAvailableForBooking(String sessionId) {
        return isAvailable && (!isReserved() || isReservedBySession(sessionId));
    }

    public String getSlotTimeRange() {
        return String.format("%02d:00-%02d:00", hourSlot, hourSlot + 1);
    }

    public boolean isExpiredReservation() {
        return reservedUntil != null && reservedUntil.isBefore(Instant.now());
    }
}
