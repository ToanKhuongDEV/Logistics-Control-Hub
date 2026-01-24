package com.logistics.hub.feature.routing.entity;

import com.logistics.hub.feature.routing.enums.RouteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing Route (Optimized route for 1 vehicle)
 * Maps to table: routes
 * Loose Coupling: vehicleId (FK -> vehicles.id)
 */
@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Vehicle ID - Loose coupling
     * Database has FK constraint -> vehicles.id
     */
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "total_distance_km", precision = 10, scale = 2)
    private BigDecimal totalDistanceKm;

    @Column(name = "total_duration_min")
    private Integer totalDurationMin;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RouteStatus status = RouteStatus.CREATED;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
