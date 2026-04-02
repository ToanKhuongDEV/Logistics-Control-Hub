package com.logistics.hub.feature.order.dto.request;

import com.logistics.hub.feature.order.constant.OrderConstant;
import com.logistics.hub.feature.order.enums.OrderStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkOrderStatusUpdateRequest {

    @NotEmpty(message = OrderConstant.ORDER_IDS_REQUIRED)
    private List<Long> orderIds;

    @NotNull(message = OrderConstant.ORDER_STATUS_REQUIRED)
    private OrderStatus status;
}
