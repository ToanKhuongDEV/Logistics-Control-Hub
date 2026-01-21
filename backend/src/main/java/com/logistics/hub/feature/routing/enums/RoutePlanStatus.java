package com.logistics.hub.feature.routing.enums;

/**
 * Trạng thái kế hoạch tuyến đường
 */
public enum RoutePlanStatus {
    /**
     * Kế hoạch mới tạo
     */
    DRAFT,

    /**
     * Đang hoạt động
     */
    ACTIVE,

    /**
     * Đã hoàn thành
     */
    COMPLETED,

    /**
     * Bị hủy/thay thế
     */
    SUPERSEDED,

    /**
     * Thất bại
     */
    FAILED
}
