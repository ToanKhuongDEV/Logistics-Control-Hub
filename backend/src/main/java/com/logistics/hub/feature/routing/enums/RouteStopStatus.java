package com.logistics.hub.feature.routing.enums;

/**
 * Trạng thái điểm dừng
 */
public enum RouteStopStatus {
    /**
     * Chưa bắt đầu
     */
    PENDING,

    /**
     * Đang đến
     */
    EN_ROUTE,

    /**
     * Đã đến
     */
    ARRIVED,

    /**
     * Hoàn thành
     */
    COMPLETED,

    /**
     * Bỏ qua
     */
    SKIPPED,

    /**
     * Thất bại
     */
    FAILED
}
