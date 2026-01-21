package com.logistics.hub.feature.routing.entity;

import com.logistics.hub.feature.routing.enums.RoutePlanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing RoutePlan (Route Plan)
 * Optimization result for 1 vehicle
 * Loose Coupling: optimizationRunId, vehicleId
 */
@Entity
@Table(name = "route_plans",
    indexes = {
        @Index(name = "idx_route_optimization", columnList = "optimization_run_id"),
        @Index(name = "idx_route_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_route_status", columnList = "status"),
        @Index(name = "idx_route_version", columnList = "version")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optimization Run ID - Loose coupling
     * Database has FK constraint -> optimization_runs.id
     */
    @NotNull(message = "Optimization Run ID is required")
    @Column(name = "optimization_run_id", nullable = false)
    private Long optimizationRunId;

    /**
     * Vehicle ID - Loose coupling
     * Database has FK constraint -> vehicles.id
     */
    @NotNull(message = "Vehicle ID is required")
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @NotNull(message = "Planned start time is required")
    @Column(name = "planned_start_time", nullable = false)
    private Instant plannedStartTime;

    @NotNull(message = "Planned end time is required")
    @Column(name = "planned_end_time", nullable = false)
    private Instant plannedEndTime;

    @Min(value = 0, message = "Total distance must be >= 0")
    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    @Min(value = 0, message = "Total duration must be >= 0")
    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes;

    @NotNull(message = "Version is required")
    @Column(nullable = false)
    private Integer version = 1;

    @NotNull(message = "Is active is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoutePlanStatus status = RoutePlanStatus.DRAFT;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}

