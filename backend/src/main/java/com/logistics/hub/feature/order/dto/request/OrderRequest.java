package com.logistics.hub.feature.order.dto.request;

import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.constant.OrderConstant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class OrderRequest {
    
    private String code;

    @NotNull(message = OrderConstant.DELIVERY_LOCATION_DETAILS_REQUIRED)
    @Valid
    private com.logistics.hub.feature.location.dto.request.LocationRequest deliveryLocation;

    private Integer weightKg;

    private BigDecimal volumeM3;
    
    private Long driverId;

    private OrderStatus status;
}
