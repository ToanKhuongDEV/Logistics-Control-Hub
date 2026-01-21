package com.logistics.hub.feature.routing.entity;

import com.logistics.hub.feature.routing.enums.OptimizationStatus;
import com.logistics.hub.feature.routing.enums.OptimizationTriggerType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * Entity representing OptimizationRun (AI Optimization Run)
 * Stores history and traces AI efficiency
 */
@Entity
@Table(name = "optimization_runs",
    indexes = {
        @Index(name = "idx_optimization_trigger", columnList = "trigger_type"),
        @Index(name = "idx_optimization_status", columnList = "status"),
        @Index(name = "idx_optimization_created", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Trigger type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private OptimizationTriggerType triggerType;

    @Column(name = "trigger_reason", length = 500)
    private String triggerReason;

    /**
     * Input data snapshot (JSON)
     * Example: {orders: [...], vehicles: [...], constraints: {...}}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> inputSnapshot;

    /**
     * Output metrics (JSON)
     * Example: {totalDistance: 100, totalTime: 200, vehiclesUsed: 5}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_metrics", columnDefinition = "jsonb")
    private Map<String, Object> outputMetrics;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "completed_at")
    private Instant completedAt;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OptimizationStatus status = OptimizationStatus.PENDING;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}

