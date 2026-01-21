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
 * Entity đại diện cho Vehicle (Xe giao hàng)
 * Sử dụng Loose Coupling: lưu depotId và driverId thay vì entity references
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

    @NotBlank(message = "Biển số xe không được để trống")
    @Column(nullable = false, unique = true, length = 20)
    private String plateNumber;

    /**
     * Depot ID - Loose coupling: Không dùng @ManyToOne
     * Database vẫn có FK constraint để đảm bảo tính toàn vẹn
     */
    @NotNull(message = "Depot ID không được để trống")
    @Column(nullable = false)
    private Long depotId;

    /**
     * Driver ID - Loose coupling: Có thể null (xe chưa có tài xế)
     * Database vẫn có FK constraint khi không null
     */
    @Column(nullable = true)
    private Long driverId;

    @Min(value = 0, message = "Tải trọng phải lớn hơn hoặc bằng 0")
    @NotNull(message = "Tải trọng không được để trống")
    @Column(nullable = false)
    private Double capacity;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitude", column = @Column(name = "current_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "current_longitude"))
    })
    private Location currentLocation;

    @NotNull(message = "Trạng thái xe không được để trống")
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
