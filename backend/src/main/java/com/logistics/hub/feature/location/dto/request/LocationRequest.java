package com.logistics.hub.feature.location.dto.request;

import com.logistics.hub.feature.location.constant.LocationConstant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationRequest {
    @NotBlank(message = LocationConstant.STREET_REQUIRED)
    private String street;

    @NotBlank(message = LocationConstant.CITY_REQUIRED)
    private String city;

    @NotBlank(message = LocationConstant.COUNTRY_REQUIRED)
    private String country;
}
