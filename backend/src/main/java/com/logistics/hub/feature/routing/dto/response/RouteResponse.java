package com.logistics.hub.feature.routing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho một route (lộ trình của một xe)
 * Chứa thông tin về tuyến đường và danh sách các điểm dừng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    /**
     * ID của route
     */
    private Long id;

    /**
     * ID của vehicle được gán cho route này
     */
    private Long vehicleId;

    /**
     * Trạng thái của route (CREATED, IN_PROGRESS, COMPLETED, CANCELLED)
     */
    private String status;

    /**
     * Tổng khoảng cách của route (km)
     */
    private BigDecimal totalDistanceKm;

    /**
     * Tổng thời gian dự kiến của route (phút)
     */
    private Integer totalDurationMin;

    /**
     * Tổng chi phí của route (tính theo km)
     */
    private BigDecimal totalCost;

    /**
     * Encoded polyline geometry cho route (để vẽ trên bản đồ)
     */
    private String polyline;

    /**
     * Danh sách các điểm dừng trong route (đã sắp xếp theo thứ tự)
     */
    private List<RouteStopResponse> stops;
}
