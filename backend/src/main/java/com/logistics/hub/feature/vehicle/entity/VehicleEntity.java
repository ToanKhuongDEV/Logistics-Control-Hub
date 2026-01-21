package com.logistics.hub.feature.vehicle.entity;

import com.logistics.hub.common.valueobject.Location;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing Vehicle (Delivery Vehicle)
 * Uses Loose Coupling: stores depotId and driverId instead of entity references
 */
@Entity
@Table(name = "vehicles", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_vehicle_plate", columnNames = "plate_number")
    },
    indexes = {
        @Index(name = "idx_vehicle_depot", columnList = "depot_id"),
        @Index(name = "idx_vehicle_driver", columnList = "driver_id"),
        @Index(name = "idx_vehicle_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Plate number is required")
    @Column(nullable = false, unique = true, length = 20)
    private String plateNumber;

    /**
     * Depot ID - Loose coupling: No @ManyToOne
     * Database still has FK constraint for data integrity
     */
    @NotNull(message = "Depot ID is required")
    @Column(nullable = false)
    private Long depotId;

    /**
     * Driver ID - Loose coupling: Can be null (vehicle without driver)
     * Database still has FK constraint when not null
     */
    @Column(nullable = true)
    private Long driverId;

    @Min(value = 0, message = "Capacity must be greater than or equal to 0")
    @NotNull(message = "Capacity is required")
    @Column(nullable = false)
    private Double capacity;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitude", column = @Column(name = "current_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "current_longitude"))
    })
    private Location currentLocation;

    @NotNull(message = "Vehicle status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}

