package com.logistics.hub.feature.routing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopResponse {

    private Long id;
    private Long locationId;
    private Long orderId;
    private Integer stopSequence;
    private BigDecimal distanceFromPrevKm;
    private Integer durationFromPrevMin;
    private Double latitude;
    private Double longitude;
}
