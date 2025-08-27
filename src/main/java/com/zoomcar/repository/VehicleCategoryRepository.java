package com.zoomcar.repository;

import com.zoomcar.entity.VehicleCategory;
import com.zoomcar.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for VehicleCategory entity - vehicle category management.
 */
@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, UUID> {

    /**
     * Find category by name
     * Category lookup by name
     */
    Optional<VehicleCategory> findByNameIgnoreCase(String name);

    /**
     * Find all active categories
     * Active category list for UI
     */
    @Query("SELECT vc FROM VehicleCategory vc WHERE vc.isActive = true " +
           "ORDER BY vc.displayOrder ASC, vc.name ASC")
    List<VehicleCategory> findActiveCategoriesOrderedByDisplay();

    /**
     * Find categories by vehicle type
     * Type-specific category listing (CAR vs BIKE)
     */
    @Query("SELECT vc FROM VehicleCategory vc WHERE vc.vehicleType = :vehicleType " +
           "AND vc.isActive = true " +
           "ORDER BY vc.displayOrder ASC, vc.name ASC")
    List<VehicleCategory> findActiveCategoriesByType(@Param("vehicleType") VehicleType vehicleType);

    /**
     * Check if category name exists
     * Validation for category creation
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find categories with active vehicles
     * Operational categories query
     */
    @Query("SELECT DISTINCT vc FROM VehicleCategory vc " +
           "JOIN Vehicle v ON vc.id = v.categoryId " +
           "WHERE vc.isActive = true AND v.status = 'ACTIVE' " +
           "ORDER BY vc.displayOrder ASC, vc.name ASC")
    List<VehicleCategory> findCategoriesWithActiveVehicles();

    /**
     * Count vehicles per category
     * Category analytics query
     */
    @Query("SELECT vc.id, vc.name, vc.vehicleType, COUNT(v.id) as vehicleCount " +
           "FROM VehicleCategory vc LEFT JOIN Vehicle v ON vc.id = v.categoryId AND v.status = 'ACTIVE' " +
           "WHERE vc.isActive = true " +
           "GROUP BY vc.id, vc.name, vc.vehicleType " +
           "ORDER BY vehicleCount DESC, vc.name ASC")
    List<Object[]> getCategoryVehicleCounts();

    /**
     * Find categories by city (categories with vehicles in that city)
     * City-specific category availability
     */
    @Query("SELECT DISTINCT vc FROM VehicleCategory vc " +
           "JOIN Vehicle v ON vc.id = v.categoryId " +
           "WHERE vc.isActive = true AND v.status = 'ACTIVE' " +
           "AND v.cityId = :cityId " +
           "ORDER BY vc.displayOrder ASC, vc.name ASC")
    List<VehicleCategory> findCategoriesAvailableInCity(@Param("cityId") UUID cityId);

    /**
     * Find categories by hub (categories with vehicles in that hub)
     * Hub-specific category availability
     */
    @Query("SELECT DISTINCT vc FROM VehicleCategory vc " +
           "JOIN Vehicle v ON vc.id = v.categoryId " +
           "WHERE vc.isActive = true AND v.status = 'ACTIVE' " +
           "AND v.hubId = :hubId " +
           "ORDER BY vc.displayOrder ASC, vc.name ASC")
    List<VehicleCategory> findCategoriesAvailableInHub(@Param("hubId") UUID hubId);

    /**
     * Search categories by name pattern
     * Category search functionality
     */
    @Query("SELECT vc FROM VehicleCategory vc WHERE vc.isActive = true " +
           "AND LOWER(vc.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY vc.displayOrder ASC, vc.name ASC")
    List<VehicleCategory> searchCategoriesByName(@Param("searchTerm") String searchTerm);

    /**
     * Find categories by multiple IDs
     * Bulk operations support
     */
    @Query("SELECT vc FROM VehicleCategory vc WHERE vc.id IN :categoryIds " +
           "ORDER BY vc.displayOrder ASC, vc.name")
    List<VehicleCategory> findCategoriesByIds(@Param("categoryIds") List<UUID> categoryIds);

    /**
     * Get category pricing summary
     * Pricing analytics query
     */
    @Query("SELECT vc.id, vc.name, vc.vehicleType, " +
           "       MIN(vc.pricePerHour) as minPrice, " +
           "       MAX(vc.pricePerHour) as maxPrice, " +
           "       AVG(vc.pricePerHour) as avgPrice " +
           "FROM VehicleCategory vc " +
           "WHERE vc.isActive = true " +
           "GROUP BY vc.id, vc.name, vc.vehicleType " +
           "ORDER BY avgPrice ASC")
    List<Object[]> getCategoryPricingSummary();
}
