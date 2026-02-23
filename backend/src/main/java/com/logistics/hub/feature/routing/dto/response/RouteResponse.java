package com.logistics.hub.feature.routing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    private Long id;
    private Long vehicleId;
    private String status;
    private BigDecimal totalDistanceKm;
    private Integer totalDurationMin;
    private BigDecimal totalCost;
    private String polyline;
    private List<RouteStopResponse> stops;
}
