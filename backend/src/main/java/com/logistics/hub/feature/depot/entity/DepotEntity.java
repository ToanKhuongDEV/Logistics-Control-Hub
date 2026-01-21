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
 * Entity representing Depot (Warehouse/Distribution Center)
 * Uses Loose Coupling: locationId instead of embedded Location
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

    @NotBlank(message = "Depot name is required")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Location ID - Loose coupling
     * Database has FK constraint -> locations.id
     */
    @NotNull(message = "Location ID is required")
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Min(value = 1, message = "Minimum capacity is 1 vehicle")
    @Column(nullable = false)
    private Integer capacity;

    /**
     * Depot opening time (e.g., 08:00)
     */
    @Column(name = "operating_start")
    private LocalTime operatingStart;

    /**
     * Depot closing time (e.g., 18:00)
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

