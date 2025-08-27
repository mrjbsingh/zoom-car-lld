package com.zoomcar.repository;

import com.zoomcar.entity.User;
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
 * Repository for User entity with optimized queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email
     * Primary login method
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone number
     * Alternative login method
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Check if email exists
     * Registration validation
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number exists
     * Registration validation
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Find verified users
     * Active user base query
     */
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = true " +
           "AND u.isPhoneVerified = true " +
           "ORDER BY u.createdAt DESC")
    Page<User> findVerifiedUsers(Pageable pageable);

    /**
     * Find users needing verification
     * Customer support query
     */
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false " +
           "OR u.isPhoneVerified = false " +
           "ORDER BY u.createdAt DESC")
    List<User> findUsersNeedingVerification();

    /**
     * Update email verification status
     * Verification process
     */
    @Modifying
    @Query("UPDATE User u SET u.isEmailVerified = true, u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :userId")
    int markEmailAsVerified(@Param("userId") UUID userId);

    /**
     * Update phone verification status
     * Verification process
     */
    @Modifying
    @Query("UPDATE User u SET u.isPhoneVerified = true, u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :userId")
    int markPhoneAsVerified(@Param("userId") UUID userId);

    /**
     * Update user profile
     * Profile management
     */
    @Modifying
    @Query("UPDATE User u SET u.firstName = :firstName, " +
           "u.lastName = :lastName, " +
           "u.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :userId")
    int updateUserProfile(@Param("userId") UUID userId,
                         @Param("firstName") String firstName,
                         @Param("lastName") String lastName);

    /**
     * Find users by registration date range
     * Analytics query
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate " +
           "AND u.createdAt <= :endDate " +
           "ORDER BY u.createdAt DESC")
    List<User> findUsersByRegistrationDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Count users by verification status
     * Analytics query
     */
    @Query("SELECT " +
           "SUM(CASE WHEN u.isEmailVerified = true AND u.isPhoneVerified = true THEN 1 ELSE 0 END) as fullyVerified, " +
           "SUM(CASE WHEN u.isEmailVerified = true AND u.isPhoneVerified = false THEN 1 ELSE 0 END) as emailOnly, " +
           "SUM(CASE WHEN u.isEmailVerified = false AND u.isPhoneVerified = true THEN 1 ELSE 0 END) as phoneOnly, " +
           "SUM(CASE WHEN u.isEmailVerified = false AND u.isPhoneVerified = false THEN 1 ELSE 0 END) as unverified " +
           "FROM User u")
    Object[] getUserVerificationStats();

    /**
     * Find active users (users with recent bookings)
     * Customer analytics
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "JOIN Booking b ON u.id = b.userId " +
           "WHERE b.createdAt >= :sinceDate " +
           "ORDER BY u.firstName, u.lastName")
    List<User> findActiveUsersSince(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Search users by name
     * Customer support search
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY u.firstName, u.lastName")
    List<User> searchUsersByName(@Param("searchTerm") String searchTerm);

    /**
     * Find users by multiple IDs
     * Bulk operations support
     */
    @Query("SELECT u FROM User u WHERE u.id IN :userIds " +
           "ORDER BY u.firstName, u.lastName")
    List<User> findUsersByIds(@Param("userIds") List<UUID> userIds);
}
