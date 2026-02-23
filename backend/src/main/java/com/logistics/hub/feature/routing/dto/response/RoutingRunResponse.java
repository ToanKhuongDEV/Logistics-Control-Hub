package com.logistics.hub.feature.routing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRunResponse {

    private Long id;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalDistanceKm;
    private BigDecimal totalCost;
    private String configuration;
    private List<RouteResponse> routes;
    private LocalDateTime createdAt;
}
