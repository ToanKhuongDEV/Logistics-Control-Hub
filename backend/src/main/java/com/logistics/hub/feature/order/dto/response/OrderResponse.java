package com.logistics.hub.feature.order.dto.response;

import com.logistics.hub.feature.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;


@Data
public class OrderResponse {
    private Long id;
    private String code;
    private Long deliveryLocationId;
    private Integer weightKg;
    private BigDecimal volumeM3;
    private OrderStatus status;
    private Instant createdAt;
}
