package com.zoomcar.entity;

import com.zoomcar.enums.FuelType;
import com.zoomcar.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Vehicle Category entity - Economy, Premium, SUV, Bike etc.
 */
@Entity
@Table(name = "vehicle_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Economy, Premium, SUV, Bike

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType; // car, bike

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    private FuelType fuelType; // petrol, diesel, electric, hybrid

    @Column(name = "base_price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePricePerHour;

    @Column(name = "base_price_per_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePricePerKm;

    @Column(name = "security_deposit", nullable = false, precision = 10, scale = 2)
    private BigDecimal securityDeposit;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleModel> vehicleModels;
}
