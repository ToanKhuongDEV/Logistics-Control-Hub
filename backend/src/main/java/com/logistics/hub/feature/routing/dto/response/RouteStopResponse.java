package com.logistics.hub.feature.routing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO cho một route stop (điểm dừng trên chặng)
 * Chứa thông tin về một điểm giao hàng cụ thể trong route
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopResponse {

    /**
     * ID của route stop
     */
    private Long id;

    /**
     * ID của location (địa điểm giao hàng)
     */
    private Long locationId;

    /**
     * ID của order được giao tại điểm này
     */
    private Long orderId;

    /**
     * Thứ tự điểm dừng trong route (1, 2, 3,...)
     */
    private Integer stopSequence;

    /**
     * Khoảng cách từ điểm trước đó (km)
     */
    private BigDecimal distanceFromPrevKm;

    /**
     * Thời gian di chuyển từ điểm trước đó (phút)
     */
    private Integer durationFromPrevMin;
}
