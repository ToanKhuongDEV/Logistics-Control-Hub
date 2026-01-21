package com.logistics.hub.feature.location.entity;

import com.logistics.hub.feature.location.enums.LocationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
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
 * Entity representing Location (Normalized geographic point)
 * Purpose: Avoid lat-long duplication, normalize addresses
 */
@Entity
@Table(name = "locations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_location_name", columnNames = "name")
    },
    indexes = {
        @Index(name = "idx_location_type", columnList = "type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Location name is required")
    @Column(nullable = false, unique = true, length = 200)
    private String name;

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

    @NotNull(message = "Location type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LocationType type;

    @Column(length = 500)
    private String address;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}

