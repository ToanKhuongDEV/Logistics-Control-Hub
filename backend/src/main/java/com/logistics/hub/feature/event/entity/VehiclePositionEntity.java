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
 * Entity đại diện cho VehiclePosition (Vị trí xe theo thời gian)
 * Time-series data từ GPS/Kafka
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
     * Database có FK constraint → vehicles.id
     */
    @NotNull(message = "Vehicle ID không được để trống")
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Min(value = -90, message = "Latitude phải từ -90 đến 90")
    @Max(value = 90, message = "Latitude phải từ -90 đến 90")
    @NotNull(message = "Latitude không được để trống")
    @Column(nullable = false)
    private Double latitude;

    @Min(value = -180, message = "Longitude phải từ -180 đến 180")
    @Max(value = 180, message = "Longitude phải từ -180 đến 180")
    @NotNull(message = "Longitude không được để trống")
    @Column(nullable = false)
    private Double longitude;

    @Min(value = 0, message = "Tốc độ phải >= 0")
    @Column(name = "speed_kmh")
    private Double speedKmh;

    @Min(value = 0, message = "Heading phải từ 0 đến 360")
    @Max(value = 360, message = "Heading phải từ 0 đến 360")
    @Column(name = "heading_degrees")
    private Double headingDegrees;

    @NotNull(message = "Timestamp không được để trống")
    @Column(nullable = false)
    private Instant timestamp;

    @NotNull(message = "Source không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PositionSource source = PositionSource.GPS;

    @Column(name = "accuracy_meters")
    private Double accuracyMeters;
}
