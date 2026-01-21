package com.logistics.hub.feature.event.entity;

import com.logistics.hub.feature.event.enums.PositionSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entity representing VehiclePosition (Vehicle position over time)
 * Time-series data from GPS/Kafka
 * Loose Coupling: vehicleId
 */
@Entity
@Table(name = "vehicle_positions",
    indexes = {
        @Index(name = "idx_position_vehicle_time", columnList = "vehicle_id, timestamp DESC"),
        @Index(name = "idx_position_timestamp", columnList = "timestamp DESC")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Vehicle ID - Loose coupling
     * Database has FK constraint -> vehicles.id
     */
    @NotNull(message = "Vehicle ID is required")
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    @NotNull(message = "Latitude is required")
    @Column(nullable = false)
    private Double latitude;

    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    @NotNull(message = "Longitude is required")
    @Column(nullable = false)
    private Double longitude;

    @Min(value = 0, message = "Speed must be >= 0")
    @Column(name = "speed_kmh")
    private Double speedKmh;

    @Min(value = 0, message = "Heading must be between 0 and 360")
    @Max(value = 360, message = "Heading must be between 0 and 360")
    @Column(name = "heading_degrees")
    private Double headingDegrees;

    @NotNull(message = "Timestamp is required")
    @Column(nullable = false)
    private Instant timestamp;

    @NotNull(message = "Source is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PositionSource source = PositionSource.GPS;

    @Column(name = "accuracy_meters")
    private Double accuracyMeters;
}

