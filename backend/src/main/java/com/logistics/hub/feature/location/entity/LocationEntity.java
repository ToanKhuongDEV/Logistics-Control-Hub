package com.logistics.hub.feature.location.entity;

import com.logistics.hub.common.base.BaseEntity;
import jakarta.persistence.*;
import com.logistics.hub.feature.location.constant.LocationConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;

@Entity
@Table(name = "locations")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationEntity extends BaseEntity {

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LocationEntity other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
