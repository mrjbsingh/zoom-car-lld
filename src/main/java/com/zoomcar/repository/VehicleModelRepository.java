package com.zoomcar.repository;

import com.zoomcar.entity.VehicleModel;
import com.zoomcar.enums.FuelType;
import com.zoomcar.enums.TransmissionType;
import com.zoomcar.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for VehicleModel entity - vehicle model management.
 */
@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, UUID> {

    /**
     * Find model by name and brand
     * Model lookup by name and brand
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE vm.categoryId = :categoryId " +
           "AND LOWER(vm.brand) = LOWER(:brand) " +
           "AND LOWER(vm.model) = LOWER(:model)")
    Optional<VehicleModel> findByBrandAndModel(@Param("categoryId") UUID categoryId,
                                              @Param("brand") String brand,
                                              @Param("model") String model);

    /**
     * Find all active models
     * Active model list for UI
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE vm.isActive = true " +
           "ORDER BY vm.brand ASC, vm.model ASC")
    List<VehicleModel> findActiveModelsOrderedByBrand();

    /**
     * Find models by category
     * Category-specific model listing
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE vm.categoryId = :categoryId " +
           "AND vm.isActive = true " +
           "ORDER BY vm.brand ASC, vm.model ASC")
    List<VehicleModel> findActiveModelsByCategory(@Param("categoryId") UUID categoryId);

    /**
     * Find models by vehicle type
     * Type-specific model listing (CAR vs BIKE)
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE vm.vehicleType = :vehicleType " +
           "AND vm.isActive = true " +
           "ORDER BY vm.brand ASC, vm.model ASC")
    List<VehicleModel> findActiveModelsByType(@Param("vehicleType") VehicleType vehicleType);

    /**
     * Find models by fuel type
     * Fuel-specific model listing
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE vm.fuelType = :fuelType " +
           "AND vm.isActive = true " +
           "ORDER BY vm.brand ASC, vm.model ASC")
    List<VehicleModel> findActiveModelsByFuelType(@Param("fuelType") FuelType fuelType);

    /**
     * Find models by transmission type
     * Transmission-specific model listing
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE vm.transmissionType = :transmissionType " +
           "AND vm.isActive = true " +
           "ORDER BY vm.brand ASC, vm.model ASC")
    List<VehicleModel> findActiveModelsByTransmissionType(@Param("transmissionType") TransmissionType transmissionType);

    /**
     * Find models with active vehicles
     * Operational models query
     */
    @Query("SELECT DISTINCT vm FROM VehicleModel vm " +
           "JOIN Vehicle v ON vm.id = v.modelId " +
           "WHERE vm.isActive = true AND v.status = 'ACTIVE' " +
           "ORDER BY vm.brand ASC, vm.model ASC")
    List<VehicleModel> findModelsWithActiveVehicles();

    /**
     * Count vehicles per model
     * Model analytics query
     */
    @Query("SELECT vm.id, vm.brand, vm.model, vm.categoryName, COUNT(v.id) as vehicleCount " +
           "FROM VehicleModel vm LEFT JOIN Vehicle v ON vm.id = v.modelId AND v.status = 'ACTIVE' " +
           "WHERE vm.isActive = true " +
           "GROUP BY vm.id, vm.brand, vm.model, vm.categoryName " +
           "ORDER BY vehicleCount DESC, vm.brand ASC, vm.model ASC")
    List<Object[]> getModelVehicleCounts();

    /**
     * Find models by city (models with vehicles in that city)
     * City-specific model availability
     */
    @Query("SELECT DISTINCT vm FROM VehicleModel vm " +
           "JOIN Vehicle v ON vm.id = v.modelId " +
           "WHERE vm.isActive = true AND v.status = 'ACTIVE' " +
           "AND v.cityId = :cityId " +
           "ORDER BY vm.brand ASC, vm.model ASC")
    List<VehicleModel> findModelsAvailableInCity(@Param("cityId") UUID cityId);

    /**
     * Find models by hub (models with vehicles in that hub)
     * Hub-specific model availability
     */
    @Query("SELECT DISTINCT vm FROM VehicleModel vm " +
           "JOIN Vehicle v ON vm.id = v.modelId " +
           "WHERE vm.isActive = true AND v.status = 'ACTIVE' " +
           "AND v.hubId = :hubId " +
           "ORDER BY vm.brand ASC, vm.model ASC")
    List<VehicleModel> findModelsAvailableInHub(@Param("hubId") UUID hubId);

    /**
     * Search models by brand or model name
     * Model search functionality
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE vm.isActive = true " +
           "AND (LOWER(vm.brand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(vm.model) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY vm.brand ASC, vm.model ASC")
    List<VehicleModel> searchModelsByBrandOrModel(@Param("searchTerm") String searchTerm);

    /**
     * Find distinct brands
     * Brand listing for filters
     */
    @Query("SELECT DISTINCT vm.brand FROM VehicleModel vm " +
           "WHERE vm.isActive = true " +
           "ORDER BY vm.brand ASC")
    List<String> findDistinctBrands();

    /**
     * Find distinct brands by vehicle type
     * Type-specific brand listing
     */
    @Query("SELECT DISTINCT vm.brand FROM VehicleModel vm " +
           "WHERE vm.vehicleType = :vehicleType AND vm.isActive = true " +
           "ORDER BY vm.brand ASC")
    List<String> findDistinctBrandsByType(@Param("vehicleType") VehicleType vehicleType);

    /**
     * Find models by brand
     * Brand-specific model listing
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE LOWER(vm.brand) = LOWER(:brand) " +
           "AND vm.isActive = true " +
           "ORDER BY vm.model ASC")
    List<VehicleModel> findActiveModelsByBrand(@Param("brand") String brand);

    /**
     * Check if model exists
     * Validation for model creation
     */
    @Query("SELECT COUNT(vm) > 0 FROM VehicleModel vm WHERE vm.categoryId = :categoryId " +
           "AND LOWER(vm.brand) = LOWER(:brand) " +
           "AND LOWER(vm.model) = LOWER(:model)")
    boolean existsByBrandAndModel(@Param("categoryId") UUID categoryId,
                                 @Param("brand") String brand,
                                 @Param("model") String model);

    /**
     * Find models by multiple IDs
     * Bulk operations support
     */
    @Query("SELECT vm FROM VehicleModel vm WHERE vm.id IN :modelIds " +
           "ORDER BY vm.brand ASC, vm.model")
    List<VehicleModel> findModelsByIds(@Param("modelIds") List<UUID> modelIds);

    /**
     * Get model feature summary
     * Feature analytics query
     */
    @Query("SELECT vm.vehicleType, vm.fuelType, vm.transmissionType, COUNT(vm.id) as modelCount " +
           "FROM VehicleModel vm " +
           "WHERE vm.isActive = true " +
           "GROUP BY vm.vehicleType, vm.fuelType, vm.transmissionType " +
           "ORDER BY modelCount DESC")
    List<Object[]> getModelFeatureSummary();
}
