package com.zoomcar.repository;

import com.zoomcar.entity.Vehicle;
import com.zoomcar.enums.VehicleStatus;
import com.zoomcar.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Vehicle entity with optimized queries for fleet management.
 * Uses UUID-only relationships to avoid JOINs and improve performance.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    /**
     * Find vehicles by hub ID
     * Uses denormalized hub fields to avoid JOINs
     */
    @Query("SELECT v FROM Vehicle v WHERE v.hubId = :hubId " +
           "AND v.status = 'ACTIVE' " +
           "ORDER BY v.registrationNumber ASC")
    List<Vehicle> findActiveVehiclesByHub(@Param("hubId") UUID hubId);

    /**
     * Find vehicles by city ID
     * Uses denormalized city fields to avoid JOINs
     */
    @Query("SELECT v FROM Vehicle v WHERE v.cityId = :cityId " +
           "AND v.status = 'ACTIVE' " +
           "ORDER BY v.hubName, v.registrationNumber ASC")
    Page<Vehicle> findActiveVehiclesByCity(@Param("cityId") UUID cityId, Pageable pageable);

    /**
     * Find vehicles by category ID
     * Uses denormalized category fields to avoid JOINs
     */
    @Query("SELECT v FROM Vehicle v WHERE v.categoryId = :categoryId " +
           "AND v.status = 'ACTIVE' " +
           "ORDER BY v.cityName, v.hubName ASC")
    List<Vehicle> findActiveVehiclesByCategory(@Param("categoryId") UUID categoryId);

    /**
     * Find vehicles by model ID
     * Uses denormalized model fields to avoid JOINs
     */
    @Query("SELECT v FROM Vehicle v WHERE v.modelId = :modelId " +
           "AND v.status = 'ACTIVE' " +
           "ORDER BY v.cityName, v.hubName ASC")
    List<Vehicle> findActiveVehiclesByModel(@Param("modelId") UUID modelId);

    /**
     * Find vehicle by registration number
     * Unique identifier for vehicle lookup
     */
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    /**
     * Find vehicles by status
     * Fleet management query
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status = :status " +
           "ORDER BY v.cityName, v.hubName, v.registrationNumber ASC")
    Page<Vehicle> findVehiclesByStatus(@Param("status") VehicleStatus status, Pageable pageable);

    /**
     * Find vehicles by type and city
     * Type-specific fleet queries (CAR vs BIKE)
     */
    @Query("SELECT v FROM Vehicle v WHERE v.vehicleType = :vehicleType " +
           "AND v.cityId = :cityId " +
           "AND v.status = 'ACTIVE' " +
           "ORDER BY v.hubName, v.categoryName ASC")
    List<Vehicle> findActiveVehiclesByTypeAndCity(@Param("vehicleType") VehicleType vehicleType,
                                                 @Param("cityId") UUID cityId);

    /**
     * Update vehicle status
     * Fleet management operation
     */
    @Modifying
    @Query("UPDATE Vehicle v SET v.status = :status, v.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE v.id = :vehicleId")
    int updateVehicleStatus(@Param("vehicleId") UUID vehicleId,
                           @Param("status") VehicleStatus status);

    /**
     * Update vehicle hub assignment
     * Fleet rebalancing operation
     */
    @Modifying
    @Query("UPDATE Vehicle v SET v.hubId = :newHubId, " +
           "v.hubName = :newHubName, " +
           "v.cityId = :newCityId, " +
           "v.cityName = :newCityName, " +
           "v.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE v.id = :vehicleId")
    int updateVehicleHub(@Param("vehicleId") UUID vehicleId,
                        @Param("newHubId") UUID newHubId,
                        @Param("newHubName") String newHubName,
                        @Param("newCityId") UUID newCityId,
                        @Param("newCityName") String newCityName);

    /**
     * Count vehicles by hub and status
     * Fleet analytics query
     */
    @Query("SELECT v.hubId, v.hubName, v.status, COUNT(v) " +
           "FROM Vehicle v " +
           "WHERE v.cityId = :cityId " +
           "GROUP BY v.hubId, v.hubName, v.status " +
           "ORDER BY v.hubName, v.status")
    List<Object[]> countVehiclesByHubAndStatus(@Param("cityId") UUID cityId);

    /**
     * Count vehicles by category and status
     * Fleet composition analytics
     */
    @Query("SELECT v.categoryId, v.categoryName, v.vehicleType, v.status, COUNT(v) " +
           "FROM Vehicle v " +
           "WHERE v.cityId = :cityId " +
           "GROUP BY v.categoryId, v.categoryName, v.vehicleType, v.status " +
           "ORDER BY v.categoryName, v.status")
    List<Object[]> countVehiclesByCategoryAndStatus(@Param("cityId") UUID cityId);

    /**
     * Find vehicles needing maintenance
     * Operations query for maintenance scheduling
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'MAINTENANCE_REQUIRED' " +
           "ORDER BY v.lastMaintenanceDate ASC")
    List<Vehicle> findVehiclesNeedingMaintenance();

    /**
     * Find vehicles by multiple IDs
     * Bulk operations support
     */
    @Query("SELECT v FROM Vehicle v WHERE v.id IN :vehicleIds " +
           "ORDER BY v.cityName, v.hubName, v.registrationNumber")
    List<Vehicle> findVehiclesByIds(@Param("vehicleIds") List<UUID> vehicleIds);

    /**
     * Search vehicles by registration number pattern
     * Support search functionality
     */
    @Query("SELECT v FROM Vehicle v WHERE v.registrationNumber LIKE %:pattern% " +
           "ORDER BY v.registrationNumber ASC")
    List<Vehicle> searchByRegistrationNumber(@Param("pattern") String pattern);

    /**
     * Fleet utilization report
     * Analytics query for vehicle usage
     */
    @Query("SELECT v.id, v.registrationNumber, v.categoryName, v.hubName, " +
           "       COUNT(b.id) as totalBookings " +
           "FROM Vehicle v LEFT JOIN Booking b ON v.id = b.vehicleId " +
           "WHERE v.cityId = :cityId " +
           "AND v.status = 'ACTIVE' " +
           "GROUP BY v.id, v.registrationNumber, v.categoryName, v.hubName " +
           "ORDER BY totalBookings DESC")
    List<Object[]> getFleetUtilizationReport(@Param("cityId") UUID cityId);
}
