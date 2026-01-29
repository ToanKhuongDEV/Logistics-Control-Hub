package com.logistics.hub.feature.order.dto.request;

import com.logistics.hub.feature.order.constant.OrderConstant;
import com.logistics.hub.feature.order.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;


@Data
public class OrderRequest {
    
    @NotBlank(message = OrderConstant.ORDER_CODE_REQUIRED)
    private String code;

    @NotNull(message = "Delivery location details are required")
    @Valid
    private com.logistics.hub.feature.location.dto.request.LocationRequest deliveryLocation;

    private Integer weightKg;

    private BigDecimal volumeM3;

    private OrderStatus status;
}
