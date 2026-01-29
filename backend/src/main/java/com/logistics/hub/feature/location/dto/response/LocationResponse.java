package com.logistics.hub.feature.location.dto.response;

import lombok.Data;

@Data
public class LocationResponse {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
}
