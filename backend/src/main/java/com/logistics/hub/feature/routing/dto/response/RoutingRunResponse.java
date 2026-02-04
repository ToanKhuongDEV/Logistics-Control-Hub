package com.logistics.hub.feature.routing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho một routing run (một lần chạy tối ưu hóa)
 * Chứa thông tin tổng quan và danh sách các routes đã được tối ưu hóa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRunResponse {

    /**
     * ID của routing run
     */
    private Long id;

    /**
     * Trạng thái của routing run (PENDING, RUNNING, COMPLETED, FAILED)
     */
    private String status;

    /**
     * Thời điểm bắt đầu chạy optimization
     */
    private LocalDateTime startTime;

    /**
     * Thời điểm kết thúc optimization
     */
    private LocalDateTime endTime;

    /**
     * Tổng khoảng cách của tất cả các routes (km)
     */
    private BigDecimal totalDistanceKm;

    /**
     * Tổng chi phí của tất cả các routes
     */
    private BigDecimal totalCost;

    /**
     * Cấu hình đã sử dụng cho optimization (solver settings, thời gian giải,...)
     */
    private String configuration;

    /**
     * Danh sách các routes đã được tối ưu hóa
     */
    private List<RouteResponse> routes;

    /**
     * Thời điểm tạo routing run
     */
    private LocalDateTime createdAt;
}
