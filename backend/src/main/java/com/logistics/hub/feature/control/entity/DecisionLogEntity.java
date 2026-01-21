package com.logistics.hub.feature.control.entity;

import com.logistics.hub.feature.control.enums.DecisionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * Entity đại diện cho DecisionLog (Giải thích quyết định AI)
 * Explainable AI - truy vết tại sao AI quyết định như vậy
 * Loose Coupling: optimizationRunId
 */
@Entity
@Table(name = "decision_logs",
    indexes = {
        @Index(name = "idx_decision_optimization", columnList = "optimization_run_id"),
        @Index(name = "idx_decision_type", columnList = "decision_type"),
        @Index(name = "idx_decision_timestamp", columnList = "timestamp DESC")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optimization Run ID - Loose coupling
     * Database có FK constraint → optimization_runs.id
     */
    @NotNull(message = "Optimization Run ID không được để trống")
    @Column(name = "optimization_run_id", nullable = false)
    private Long optimizationRunId;

    @NotNull(message = "Decision type không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false, length = 30)
    private DecisionType decisionType;

    @NotBlank(message = "Reason không được để trống")
    @Column(nullable = false, length = 1000)
    private String reason;

    /**
     * Các lựa chọn thay thế được AI xem xét (JSON)
     * Ví dụ: [{option: "Vehicle A", score: 0.8}, {option: "Vehicle B", score: 0.6}]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> alternatives;

    @NotBlank(message = "Selected option không được để trống")
    @Column(name = "selected_option", nullable = false, length = 500)
    private String selectedOption;

    @Min(value = 0, message = "Confidence score phải từ 0 đến 1")
    @Max(value = 1, message = "Confidence score phải từ 0 đến 1")
    @Column(name = "confidence_score")
    private Double confidenceScore;

    @NotNull(message = "Timestamp không được để trống")
    @Column(nullable = false)
    private Instant timestamp;
}
