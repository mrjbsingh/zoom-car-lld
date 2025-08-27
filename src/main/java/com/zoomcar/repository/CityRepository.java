package com.zoomcar.repository;

import com.zoomcar.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for City entity - master data management.
 */
@Repository
public interface CityRepository extends JpaRepository<City, UUID> {

    /**
     * Find city by name
     * City lookup by name
     */
    Optional<City> findByNameIgnoreCase(String name);

    /**
     * Find city by code
     * City lookup by unique code
     */
    Optional<City> findByCode(String code);

    /**
     * Find all active cities
     * Active city list for UI
     */
    @Query("SELECT c FROM City c WHERE c.isActive = true ORDER BY c.name ASC")
    List<City> findActiveCities();

    /**
     * Check if city name exists
     * Validation for city creation
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Check if city code exists
     * Validation for city creation
     */
    boolean existsByCode(String code);

    /**
     * Find cities with active hubs
     * Operational cities query
     */
    @Query("SELECT DISTINCT c FROM City c " +
           "JOIN Hub h ON c.id = h.cityId " +
           "WHERE c.isActive = true AND h.isActive = true " +
           "ORDER BY c.name ASC")
    List<City> findCitiesWithActiveHubs();

    /**
     * Search cities by name pattern
     * City search functionality
     */
    @Query("SELECT c FROM City c WHERE c.isActive = true " +
           "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.name ASC")
    List<City> searchCitiesByName(@Param("searchTerm") String searchTerm);

    /**
     * Count hubs per city
     * Analytics query
     */
    @Query("SELECT c.id, c.name, COUNT(h.id) as hubCount " +
           "FROM City c LEFT JOIN Hub h ON c.id = h.cityId AND h.isActive = true " +
           "WHERE c.isActive = true " +
           "GROUP BY c.id, c.name " +
           "ORDER BY hubCount DESC, c.name ASC")
    List<Object[]> getCityHubCounts();
}
