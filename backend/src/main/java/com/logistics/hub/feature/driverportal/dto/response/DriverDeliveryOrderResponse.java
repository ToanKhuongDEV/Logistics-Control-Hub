package com.logistics.hub.feature.driverportal.dto.response;

import com.logistics.hub.feature.order.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class DriverDeliveryOrderResponse {

    private Long id;
    private String code;
    private String deliveryLocationName;
    private String deliveryStreet;
    private String deliveryCity;
    private String deliveryCountry;
    private Integer weightKg;
    private BigDecimal volumeM3;
    private Long depotId;
    private String depotName;
    private Double latitude;
    private Double longitude;
    private OrderStatus status;
    private Instant createdAt;
    private Long routeId;
    private Long stopId;
    private Integer stopSequence;
}
