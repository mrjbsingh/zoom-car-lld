package com.zoomcar.repository;

import com.zoomcar.entity.VehicleSlotAvailability;
import com.zoomcar.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for VehicleSlotAvailability entity.
 * Handles denormalized aggregated availability data for fast queries.
 * This table is optimized for sub-5ms availability responses.
 */
@Repository
public interface VehicleSlotAvailabilityRepository extends JpaRepository<VehicleSlotAvailability, UUID> {

    /**
     * Find available vehicles by city, category and time slot
     * PRIMARY AVAILABILITY QUERY - optimized for sub-5ms response
     */
    @Query("SELECT vsa FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.cityId = :cityId " +
           "AND vsa.categoryId = :categoryId " +
           "AND vsa.slotStartTime = :slotStartTime " +
           "AND vsa.slotEndTime = :slotEndTime " +
           "AND vsa.availableVehicleCount > 0 " +
           "ORDER BY vsa.hubName ASC")
    List<VehicleSlotAvailability> findAvailableVehiclesBySlot(
            @Param("cityId") UUID cityId,
            @Param("categoryId") UUID categoryId,
            @Param("slotStartTime") LocalDateTime slotStartTime,
            @Param("slotEndTime") LocalDateTime slotEndTime);

    /**
     * Find available vehicles by hub, category and time slot
     * Hub-specific availability query
     */
    @Query("SELECT vsa FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.hubId = :hubId " +
           "AND vsa.categoryId = :categoryId " +
           "AND vsa.slotStartTime = :slotStartTime " +
           "AND vsa.slotEndTime = :slotEndTime " +
           "AND vsa.availableVehicleCount > 0")
    Optional<VehicleSlotAvailability> findAvailableVehiclesByHubAndSlot(
            @Param("hubId") UUID hubId,
            @Param("categoryId") UUID categoryId,
            @Param("slotStartTime") LocalDateTime slotStartTime,
            @Param("slotEndTime") LocalDateTime slotEndTime);

    /**
     * Find available vehicles by city and vehicle type
     * Type-specific availability (CAR vs BIKE)
     */
    @Query("SELECT vsa FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.cityId = :cityId " +
           "AND vsa.vehicleType = :vehicleType " +
           "AND vsa.slotStartTime = :slotStartTime " +
           "AND vsa.slotEndTime = :slotEndTime " +
           "AND vsa.availableVehicleCount > 0 " +
           "ORDER BY vsa.pricePerHour ASC")
    List<VehicleSlotAvailability> findAvailableVehiclesByTypeAndSlot(
            @Param("cityId") UUID cityId,
            @Param("vehicleType") VehicleType vehicleType,
            @Param("slotStartTime") LocalDateTime slotStartTime,
            @Param("slotEndTime") LocalDateTime slotEndTime);

    /**
     * Find cheapest available vehicles in a city for a time slot
     * Price-optimized search
     */
    @Query("SELECT vsa FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.cityId = :cityId " +
           "AND vsa.slotStartTime = :slotStartTime " +
           "AND vsa.slotEndTime = :slotEndTime " +
           "AND vsa.availableVehicleCount > 0 " +
           "ORDER BY vsa.pricePerHour ASC")
    List<VehicleSlotAvailability> findCheapestAvailableVehicles(
            @Param("cityId") UUID cityId,
            @Param("slotStartTime") LocalDateTime slotStartTime,
            @Param("slotEndTime") LocalDateTime slotEndTime);

    /**
     * Find available vehicles within price range
     * Price-filtered availability
     */
    @Query("SELECT vsa FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.cityId = :cityId " +
           "AND vsa.slotStartTime = :slotStartTime " +
           "AND vsa.slotEndTime = :slotEndTime " +
           "AND vsa.availableVehicleCount > 0 " +
           "AND vsa.pricePerHour >= :minPrice " +
           "AND vsa.pricePerHour <= :maxPrice " +
           "ORDER BY vsa.pricePerHour ASC")
    List<VehicleSlotAvailability> findAvailableVehiclesByPriceRange(
            @Param("cityId") UUID cityId,
            @Param("slotStartTime") LocalDateTime slotStartTime,
            @Param("slotEndTime") LocalDateTime slotEndTime,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Get total available vehicle count for a city and time slot
     * Fast count query for availability summary
     */
    @Query("SELECT SUM(vsa.availableVehicleCount) FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.cityId = :cityId " +
           "AND vsa.slotStartTime = :slotStartTime " +
           "AND vsa.slotEndTime = :slotEndTime")
    Long getTotalAvailableVehicleCount(@Param("cityId") UUID cityId,
                                      @Param("slotStartTime") LocalDateTime slotStartTime,
                                      @Param("slotEndTime") LocalDateTime slotEndTime);

    /**
     * Update available vehicle count after booking/cancellation
     * Background sync job - updates denormalized counts
     */
    @Modifying
    @Query("UPDATE VehicleSlotAvailability vsa " +
           "SET vsa.availableVehicleCount = :newCount, " +
           "    vsa.availableVehicleIds = :availableVehicleIds, " +
           "    vsa.lastUpdated = :lastUpdated " +
           "WHERE vsa.hubId = :hubId " +
           "AND vsa.categoryId = :categoryId " +
           "AND vsa.slotStartTime = :slotStartTime " +
           "AND vsa.slotEndTime = :slotEndTime")
    int updateAvailableVehicleCount(@Param("hubId") UUID hubId,
                                   @Param("categoryId") UUID categoryId,
                                   @Param("slotStartTime") LocalDateTime slotStartTime,
                                   @Param("slotEndTime") LocalDateTime slotEndTime,
                                   @Param("newCount") Integer newCount,
                                   @Param("availableVehicleIds") List<UUID> availableVehicleIds,
                                   @Param("lastUpdated") LocalDateTime lastUpdated);

    /**
     * Find slots that need sync (stale data)
     * Background job query - finds slots with outdated counts
     */
    @Query("SELECT vsa FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.lastUpdated < :staleThreshold " +
           "AND vsa.slotStartTime >= :currentTime " +
           "ORDER BY vsa.lastUpdated ASC")
    List<VehicleSlotAvailability> findStaleSlotsForSync(@Param("staleThreshold") LocalDateTime staleThreshold,
                                                       @Param("currentTime") LocalDateTime currentTime);

    /**
     * Find all slots for a specific time range (for bulk sync)
     * Background job query - bulk sync operations
     */
    @Query("SELECT vsa FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.slotStartTime >= :startTime " +
           "AND vsa.slotEndTime <= :endTime " +
           "ORDER BY vsa.cityId, vsa.hubId, vsa.categoryId")
    List<VehicleSlotAvailability> findSlotsForBulkSync(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * Delete old aggregated slots (cleanup job)
     * Remove slots older than specified date to prevent table bloat
     */
    @Modifying
    @Query("DELETE FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.slotEndTime < :cutoffDate")
    int deleteOldAggregatedSlots(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * City-wise availability summary for dashboard
     * Analytics query for city-level metrics
     */
    @Query("SELECT vsa.cityId, vsa.cityName, " +
           "       SUM(vsa.availableVehicleCount) as totalAvailable, " +
           "       COUNT(DISTINCT vsa.hubId) as activeHubs, " +
           "       AVG(vsa.pricePerHour) as avgPrice " +
           "FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.slotStartTime = :slotStartTime " +
           "AND vsa.slotEndTime = :slotEndTime " +
           "AND vsa.availableVehicleCount > 0 " +
           "GROUP BY vsa.cityId, vsa.cityName " +
           "ORDER BY totalAvailable DESC")
    List<Object[]> getCityWiseAvailabilitySummary(@Param("slotStartTime") LocalDateTime slotStartTime,
                                                 @Param("slotEndTime") LocalDateTime slotEndTime);

    /**
     * Hub-wise availability summary for operations
     * Analytics query for hub-level metrics
     */
    @Query("SELECT vsa.hubId, vsa.hubName, vsa.cityName, " +
           "       SUM(vsa.availableVehicleCount) as totalAvailable, " +
           "       COUNT(DISTINCT vsa.categoryId) as activeCategories, " +
           "       MIN(vsa.pricePerHour) as minPrice, " +
           "       MAX(vsa.pricePerHour) as maxPrice " +
           "FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.cityId = :cityId " +
           "AND vsa.slotStartTime = :slotStartTime " +
           "AND vsa.slotEndTime = :slotEndTime " +
           "AND vsa.availableVehicleCount > 0 " +
           "GROUP BY vsa.hubId, vsa.hubName, vsa.cityName " +
           "ORDER BY totalAvailable DESC")
    List<Object[]> getHubWiseAvailabilitySummary(@Param("cityId") UUID cityId,
                                                @Param("slotStartTime") LocalDateTime slotStartTime,
                                                @Param("slotEndTime") LocalDateTime slotEndTime);

    /**
     * Category-wise availability and pricing
     * Analytics query for category performance
     */
    @Query("SELECT vsa.categoryId, vsa.categoryName, vsa.vehicleType, " +
           "       SUM(vsa.availableVehicleCount) as totalAvailable, " +
           "       AVG(vsa.pricePerHour) as avgPrice, " +
           "       COUNT(DISTINCT vsa.hubId) as availableHubs " +
           "FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.cityId = :cityId " +
           "AND vsa.slotStartTime = :slotStartTime " +
           "AND vsa.slotEndTime = :slotEndTime " +
           "AND vsa.availableVehicleCount > 0 " +
           "GROUP BY vsa.categoryId, vsa.categoryName, vsa.vehicleType " +
           "ORDER BY totalAvailable DESC")
    List<Object[]> getCategoryWiseAvailability(@Param("cityId") UUID cityId,
                                              @Param("slotStartTime") LocalDateTime slotStartTime,
                                              @Param("slotEndTime") LocalDateTime slotEndTime);

    /**
     * Find peak demand slots (low availability)
     * Operations query for capacity planning
     */
    @Query("SELECT vsa.slotStartTime, vsa.slotEndTime, vsa.cityName, " +
           "       SUM(vsa.availableVehicleCount) as totalAvailable " +
           "FROM VehicleSlotAvailability vsa " +
           "WHERE vsa.cityId = :cityId " +
           "AND vsa.slotStartTime >= :startDate " +
           "AND vsa.slotEndTime <= :endDate " +
           "GROUP BY vsa.slotStartTime, vsa.slotEndTime, vsa.cityName " +
           "HAVING SUM(vsa.availableVehicleCount) < :lowAvailabilityThreshold " +
           "ORDER BY totalAvailable ASC")
    List<Object[]> findPeakDemandSlots(@Param("cityId") UUID cityId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      @Param("lowAvailabilityThreshold") Long lowAvailabilityThreshold);
}
