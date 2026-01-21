package com.logistics.hub.feature.control.entity;

import com.logistics.hub.feature.control.enums.OverrideType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Entity đại diện cho ManualOverride (Can thiệp thủ công)
 * Audit trail cho quyết định của con người
 */
@Entity
@Table(name = "manual_overrides",
    indexes = {
        @Index(name = "idx_override_type", columnList = "override_type"),
        @Index(name = "idx_override_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_override_user", columnList = "performed_by"),
        @Index(name = "idx_override_timestamp", columnList = "timestamp DESC")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualOverrideEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Override type không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "override_type", nullable = false, length = 30)
    private OverrideType overrideType;

    @NotBlank(message = "Entity type không được để trống")
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @NotNull(message = "Entity ID không được để trống")
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "original_value", length = 500)
    private String originalValue;

    @NotBlank(message = "New value không được để trống")
    @Column(name = "new_value", nullable = false, length = 500)
    private String newValue;

    @NotBlank(message = "Reason không được để trống")
    @Column(nullable = false, length = 1000)
    private String reason;

    /**
     * Performed By - User ID hoặc username
     * Loose coupling - không có FK
     */
    @NotBlank(message = "Performed by không được để trống")
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    @Column(nullable = false)
    private Boolean approved = false;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @NotNull(message = "Timestamp không được để trống")
    @Column(nullable = false)
    private Instant timestamp;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
