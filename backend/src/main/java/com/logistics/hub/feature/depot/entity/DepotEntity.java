package com.logistics.hub.feature.depot.entity;

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
import java.time.LocalTime;

/**
 * Entity đại diện cho Depot (Kho/Trung tâm phân phối)
 * Sử dụng Loose Coupling: locationId thay vì embedded Location
 */
@Entity
@Table(name = "depots", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_depot_name", columnNames = "name")
    },
    indexes = {
        @Index(name = "idx_depot_location", columnList = "location_id"),
        @Index(name = "idx_depot_active", columnList = "active")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên depot không được để trống")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Location ID - Loose coupling
     * Database có FK constraint → locations.id
     */
    @NotNull(message = "Location ID không được để trống")
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Min(value = 1, message = "Sức chứa tối thiểu là 1 xe")
    @Column(nullable = false)
    private Integer capacity;

    /**
     * Giờ mở cửa depot (e.g., 08:00)
     */
    @Column(name = "operating_start")
    private LocalTime operatingStart;

    /**
     * Giờ đóng cửa depot (e.g., 18:00)
     */
    @Column(name = "operating_end")
    private LocalTime operatingEnd;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
