package com.zoomcar.repository;

import com.zoomcar.entity.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Hub entity - pickup/drop location management.
 */
@Repository
public interface HubRepository extends JpaRepository<Hub, UUID> {

    /**
     * Find hubs by city ID
     * City-specific hub listing
     */
    @Query("SELECT h FROM Hub h WHERE h.cityId = :cityId " +
           "AND h.isActive = true " +
           "ORDER BY h.name ASC")
    List<Hub> findActiveHubsByCity(@Param("cityId") UUID cityId);

    /**
     * Find hub by name and city
     * Hub lookup by name within city
     */
    @Query("SELECT h FROM Hub h WHERE h.cityId = :cityId " +
           "AND LOWER(h.name) = LOWER(:hubName)")
    Optional<Hub> findByNameAndCity(@Param("cityId") UUID cityId, 
                                   @Param("hubName") String hubName);

    /**
     * Find all active hubs
     * Global hub listing
     */
    @Query("SELECT h FROM Hub h WHERE h.isActive = true " +
           "ORDER BY h.cityName, h.name ASC")
    List<Hub> findAllActiveHubs();

    /**
     * Find hubs within radius of coordinates
     * Location-based hub search
     */
    @Query(value = "SELECT * FROM hubs h WHERE h.is_active = true " +
           "AND ST_DWithin(ST_MakePoint(h.longitude, h.latitude)::geography, " +
           "ST_MakePoint(:longitude, :latitude)::geography, :radiusMeters) " +
           "ORDER BY ST_Distance(ST_MakePoint(h.longitude, h.latitude)::geography, " +
           "ST_MakePoint(:longitude, :latitude)::geography) ASC",
           nativeQuery = true)
    List<Hub> findHubsWithinRadius(@Param("latitude") Double latitude,
                                  @Param("longitude") Double longitude,
                                  @Param("radiusMeters") Double radiusMeters);

    /**
     * Search hubs by name pattern
     * Hub search functionality
     */
    @Query("SELECT h FROM Hub h WHERE h.isActive = true " +
           "AND LOWER(h.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY h.cityName, h.name ASC")
    List<Hub> searchHubsByName(@Param("searchTerm") String searchTerm);

    /**
     * Find hubs with vehicles available
     * Operational hubs query
     */
    @Query("SELECT DISTINCT h FROM Hub h " +
           "JOIN Vehicle v ON h.id = v.hubId " +
           "WHERE h.isActive = true AND v.status = 'ACTIVE' " +
           "ORDER BY h.cityName, h.name ASC")
    List<Hub> findHubsWithActiveVehicles();

    /**
     * Count vehicles per hub
     * Hub analytics query
     */
    @Query("SELECT h.id, h.name, h.cityName, COUNT(v.id) as vehicleCount " +
           "FROM Hub h LEFT JOIN Vehicle v ON h.id = v.hubId AND v.status = 'ACTIVE' " +
           "WHERE h.cityId = :cityId AND h.isActive = true " +
           "GROUP BY h.id, h.name, h.cityName " +
           "ORDER BY vehicleCount DESC, h.name ASC")
    List<Object[]> getHubVehicleCounts(@Param("cityId") UUID cityId);

    /**
     * Find nearest hub to coordinates
     * Location-based hub selection
     */
    @Query(value = "SELECT * FROM hubs h WHERE h.is_active = true " +
           "AND h.city_id = :cityId " +
           "ORDER BY ST_Distance(ST_MakePoint(h.longitude, h.latitude)::geography, " +
           "ST_MakePoint(:longitude, :latitude)::geography) ASC " +
           "LIMIT 1",
           nativeQuery = true)
    Optional<Hub> findNearestHub(@Param("cityId") UUID cityId,
                                @Param("latitude") Double latitude,
                                @Param("longitude") Double longitude);

    /**
     * Check if hub name exists in city
     * Validation for hub creation
     */
    @Query("SELECT COUNT(h) > 0 FROM Hub h WHERE h.cityId = :cityId " +
           "AND LOWER(h.name) = LOWER(:hubName)")
    boolean existsByNameAndCity(@Param("cityId") UUID cityId, 
                               @Param("hubName") String hubName);

    /**
     * Find hubs by multiple IDs
     * Bulk operations support
     */
    @Query("SELECT h FROM Hub h WHERE h.id IN :hubIds " +
           "ORDER BY h.cityName, h.name")
    List<Hub> findHubsByIds(@Param("hubIds") List<UUID> hubIds);
}
