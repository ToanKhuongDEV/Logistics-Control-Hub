package com.logistics.hub.feature.vehicle.entity;

import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "max_weight_kg")
    private Integer maxWeightKg;

    @Column(name = "max_volume_m3", precision = 6, scale = 2)
    private BigDecimal maxVolumeM3;

    @Column(name = "cost_per_km", precision = 10, scale = 2)
    private BigDecimal costPerKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(length = 100)
    private String type;

    @Column(name = "driver_id")
    private Long driverId;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
