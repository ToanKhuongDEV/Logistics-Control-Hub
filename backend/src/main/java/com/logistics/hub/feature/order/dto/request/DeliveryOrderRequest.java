package com.logistics.hub.feature.order.dto.request;

import com.logistics.hub.common.valueobject.TimeWindow;
import com.logistics.hub.feature.order.constant.OrderConstant;
import com.logistics.hub.feature.order.enums.DeliveryOrderStatus;
import com.logistics.hub.feature.order.enums.OrderPriority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryOrderRequest {
    @NotBlank(message = OrderConstant.ORDER_NUMBER_REQUIRED)
    private String orderNumber;

    @NotNull(message = OrderConstant.CUSTOMER_ID_REQUIRED)
    private Long customerId;

    @NotNull(message = OrderConstant.PICKUP_LOCATION_REQUIRED)
    private Long pickupLocationId;

    @NotNull(message = OrderConstant.DELIVERY_LOCATION_REQUIRED)
    private Long deliveryLocationId;

    @NotNull(message = OrderConstant.DELIVERY_TIME_REQUIRED)
    private TimeWindow deliveryTimeWindow;

    private TimeWindow pickupTimeWindow;

    @NotNull(message = OrderConstant.WEIGHT_REQUIRED)
    @Min(value = 0)
    private Double weight;

    @NotNull(message = OrderConstant.PRIORITY_REQUIRED)
    private OrderPriority priority;

    @NotNull(message = OrderConstant.STATUS_REQUIRED)
    private DeliveryOrderStatus status;

    private String notes;
}
