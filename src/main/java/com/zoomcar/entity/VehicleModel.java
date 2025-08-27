package com.zoomcar.entity;

import com.zoomcar.enums.TransmissionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Vehicle Model entity - Specific models like XUV300, Swift etc.
 */
@Entity
@Table(name = "vehicle_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "model_id")
    private UUID modelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private VehicleCategory category;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", length = 20)
    private TransmissionType transmission;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> features = Map.of();

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    // Relationships
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Vehicle> vehicles;
}
