package com.logistics.hub.feature.routing.enums;

/**
 * Kiểu trigger tối ưu hóa
 */
public enum OptimizationTriggerType {
    /**
     * Tối ưu theo lịch trình (hàng ngày/giờ)
     */
    SCHEDULED,

    /**
     * Tối ưu do sự cố
     */
    DISRUPTION,

    /**
     * Can thiệp thủ công
     */
    MANUAL,

    /**
     * Đơn hàng mới
     */
    NEW_ORDERS
}
