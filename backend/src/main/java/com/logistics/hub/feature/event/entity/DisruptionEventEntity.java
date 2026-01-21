package com.logistics.hub.feature.event.entity;

import com.logistics.hub.feature.event.enums.DisruptionStatus;
import com.logistics.hub.feature.event.enums.DisruptionType;
import com.logistics.hub.feature.event.enums.SeverityLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity đại diện cho DisruptionEvent (Sự cố vận hành)
 * Trigger re-optimization
 * Loose Coupling: affectedVehicleId, affectedRouteId, locationId
 */
@Entity
@Table(name = "disruption_events",
    indexes = {
        @Index(name = "idx_disruption_type", columnList = "type"),
        @Index(name = "idx_disruption_vehicle", columnList = "affected_vehicle_id"),
        @Index(name = "idx_disruption_route", columnList = "affected_route_id"),
        @Index(name = "idx_disruption_status_severity", columnList = "status, severity"),
        @Index(name = "idx_disruption_detected", columnList = "detected_at DESC")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisruptionEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Disruption type không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DisruptionType type;

    /**
     * Affected Vehicle ID - Loose coupling (nullable)
     */
    @Column(name = "affected_vehicle_id")
    private Long affectedVehicleId;

    /**
     * Affected Route ID - Loose coupling (nullable)
     */
    @Column(name = "affected_route_id")
    private Long affectedRouteId;

    /**
     * Location ID - Loose coupling (nullable - nơi xảy ra sự cố)
     */
    @Column(name = "location_id")
    private Long locationId;

    @NotNull(message = "Severity không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeverityLevel severity;

    @NotBlank(message = "Description không được để trống")
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull(message = "Detected at không được để trống")
    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @NotNull(message = "Status không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisruptionStatus status = DisruptionStatus.DETECTED;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
