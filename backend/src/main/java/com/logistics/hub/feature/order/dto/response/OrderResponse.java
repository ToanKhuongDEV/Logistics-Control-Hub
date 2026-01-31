package com.logistics.hub.feature.order.dto.response;

import com.logistics.hub.feature.location.dto.response.LocationResponse;
import com.logistics.hub.feature.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;


@Data
public class OrderResponse {
    private Long id;
    private String code;
    private Long deliveryLocationId;
    private LocationResponse deliveryLocation;
    private Integer weightKg;
    private BigDecimal volumeM3;
    private OrderStatus status;
    private Instant createdAt;
}
