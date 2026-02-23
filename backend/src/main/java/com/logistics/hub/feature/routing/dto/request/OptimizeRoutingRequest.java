package com.logistics.hub.feature.routing.dto.request;

import com.logistics.hub.feature.routing.constant.RoutingConstant;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizeRoutingRequest {

    @NotNull(message = RoutingConstant.ORDER_IDS_REQUIRED)
    @NotEmpty(message = RoutingConstant.ORDER_IDS_EMPTY)
    private List<Long> orderIds;

    @NotNull(message = RoutingConstant.VEHICLE_IDS_REQUIRED)
    @NotEmpty(message = RoutingConstant.VEHICLE_IDS_EMPTY)
    private List<Long> vehicleIds;
}
