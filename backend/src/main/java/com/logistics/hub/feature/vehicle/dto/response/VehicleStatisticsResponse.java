package com.logistics.hub.feature.vehicle.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleStatisticsResponse {
    private long total;
    private long active;
    private long maintenance;
    private long idle;
    private BigDecimal averageCostPerKm;
    private Long totalCapacityKg;
    private BigDecimal totalCapacityM3;
}
