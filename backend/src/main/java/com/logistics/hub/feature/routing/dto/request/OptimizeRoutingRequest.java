package com.logistics.hub.feature.routing.dto.request;

import com.logistics.hub.feature.routing.constant.RoutingConstant;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO cho endpoint optimize routing
 * Chứa danh sách IDs của orders và vehicles cần tối ưu hóa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptimizeRoutingRequest {

    /**
     * Danh sách ID của các đơn hàng cần giao
     * Tất cả orders phải tồn tại trong database
     */
    @NotNull(message = RoutingConstant.ORDER_IDS_REQUIRED)
    @NotEmpty(message = RoutingConstant.ORDER_IDS_EMPTY)
    private List<Long> orderIds;

    /**
     * Danh sách ID của các phương tiện có thể sử dụng
     * Tất cả vehicles phải tồn tại trong database và thuộc cùng một depot
     */
    @NotNull(message = RoutingConstant.VEHICLE_IDS_REQUIRED)
    @NotEmpty(message = RoutingConstant.VEHICLE_IDS_EMPTY)
    private List<Long> vehicleIds;
}
