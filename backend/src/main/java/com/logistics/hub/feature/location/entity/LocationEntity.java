package com.logistics.hub.feature.location.entity;

import jakarta.persistence.*;
import com.logistics.hub.feature.location.constant.LocationConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing Location (Geographic point)
 * Maps to table: locations
 */
@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String name;

    @Min(value = -90, message = LocationConstant.LATITUDE_RANGE)
    @Max(value = 90, message = LocationConstant.LATITUDE_RANGE)
    @NotNull(message = LocationConstant.LATITUDE_REQUIRED)
    @Column(nullable = false)
    private Double latitude;

    @Min(value = -180, message = LocationConstant.LONGITUDE_RANGE)
    @Max(value = 180, message = LocationConstant.LONGITUDE_RANGE)
    @NotNull(message = LocationConstant.LONGITUDE_REQUIRED)
    @Column(nullable = false)
    private Double longitude;
}
