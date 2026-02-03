package com.logistics.hub.feature.routing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceResult {
    private BigDecimal distanceKm;
    private Integer durationMinutes;
}
