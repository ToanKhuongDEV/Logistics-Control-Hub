package com.logistics.hub.feature.location.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationResponse {
    private Long id;
    private String street;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
}
