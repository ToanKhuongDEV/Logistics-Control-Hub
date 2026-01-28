package com.logistics.hub.feature.geocoding.dto;

import lombok.Data;

@Data
public class Coordinates {
    private Double latitude;
    private Double longitude;

    public Coordinates() {
    }

    public Coordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
