package com.logistics.hub.feature.control.enums;

/**
 * Loại quyết định AI
 */
public enum DecisionType {
    /**
     * Gán đơn hàng cho xe
     */
    ORDER_ASSIGNMENT,

    /**
     * Tính toán tuyến đường
     */
    ROUTE_CALCULATION,

    /**
     * Tính toán lại tuyến đường
     */
    ROUTE_RECALCULATION,

    /**
     * Gán lại xe khác
     */
    VEHICLE_REASSIGNMENT,

    /**
     * Thay đổi thứ tự điểm dừng
     */
    STOP_REORDERING,

    /**
     * Xử lý sự cố
     */
    DISRUPTION_HANDLING
}
