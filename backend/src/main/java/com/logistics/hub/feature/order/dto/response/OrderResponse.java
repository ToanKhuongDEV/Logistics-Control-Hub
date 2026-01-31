package com.logistics.hub.feature.order.dto.response;


import com.logistics.hub.feature.order.dto.projection.OrderProjection;
import com.logistics.hub.feature.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;


@Data
public class OrderResponse {
    private Long id;
    private String code;
    private String deliveryLocationName;
    private Integer weightKg;
    private BigDecimal volumeM3;
    private OrderStatus status;
    private Instant createdAt;

    public static OrderResponse fromProjection(OrderProjection projection) {
        OrderResponse response = new OrderResponse();
        response.setId(projection.getId());
        response.setCode(projection.getCode());
        response.setDeliveryLocationName(projection.getLocName());
        response.setWeightKg(projection.getWeightKg());
        response.setVolumeM3(projection.getVolumeM3());
        response.setStatus(OrderStatus.valueOf(projection.getStatus()));
        response.setCreatedAt(projection.getCreatedAt());
        return response;
    }
}
