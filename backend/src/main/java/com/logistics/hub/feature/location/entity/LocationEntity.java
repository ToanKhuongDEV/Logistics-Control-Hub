package com.logistics.hub.feature.location.entity;

import jakarta.persistence.*;
import com.logistics.hub.feature.location.constant.LocationConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_locations_address_coords", columnNames = { "street", "city", "country", "latitude",
                "longitude" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    @NotNull(message = "Street is required")
    private String street;

    @Column(nullable = false, length = 100)
    @NotNull(message = "City is required")
    private String city;

    @Column(nullable = false, length = 100)
    @NotNull(message = "Country is required")
    private String country;

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
