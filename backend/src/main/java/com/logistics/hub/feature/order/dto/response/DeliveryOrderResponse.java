package com.logistics.hub.feature.order.dto.response;

import com.logistics.hub.common.valueobject.TimeWindow;
import com.logistics.hub.feature.order.enums.DeliveryOrderStatus;
import com.logistics.hub.feature.order.enums.OrderPriority;
import lombok.Data;

import java.time.Instant;

@Data
public class DeliveryOrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private Long pickupLocationId;
    private Long deliveryLocationId;
    private TimeWindow deliveryTimeWindow;
    private TimeWindow pickupTimeWindow;
    private Double weight;
    private OrderPriority priority;
    private DeliveryOrderStatus status;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
