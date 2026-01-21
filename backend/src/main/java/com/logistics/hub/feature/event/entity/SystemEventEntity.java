package com.logistics.hub.feature.event.entity;

import com.logistics.hub.feature.event.enums.SeverityLevel;
import com.logistics.hub.feature.event.enums.SystemEventType;
import jakarta.persistence.*;
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
 * Entity representing SystemEvent (System Event)
 * Event sourcing for monitoring & analytics
 */
@Entity
@Table(name = "system_events",
    indexes = {
        @Index(name = "idx_event_type_time", columnList = "event_type, timestamp DESC"),
        @Index(name = "idx_event_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_event_severity", columnList = "severity"),
        @Index(name = "idx_event_timestamp", columnList = "timestamp DESC")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Event type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SystemEventType eventType;

    @NotBlank(message = "Entity type is required")
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Payload JSON - additional event data
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @NotNull(message = "Timestamp is required")
    @Column(nullable = false)
    private Instant timestamp;

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeverityLevel severity = SeverityLevel.INFO;

    @Column(length = 1000)
    private String message;
}

