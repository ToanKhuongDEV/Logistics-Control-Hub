package com.logistics.hub.feature.order.enums;

/**
 * Trạng thái thực thi giao hàng
 */
public enum DeliveryTaskStatus {
    /**
     * Task mới tạo, chưa bắt đầu
     */
    CREATED,

    /**
     * Đang tiến hành lấy hàng
     */
    PICKING_UP,

    /**
     * Đã lấy hàng, đang vận chuyển
     */
    IN_TRANSIT,

    /**
     * Đang giao hàng cho khách
     */
    DELIVERING,

    /**
     * Hoàn thành giao hàng
     */
    COMPLETED,

    /**
     * Thất bại
     */
    FAILED,

    /**
     * Bị hủy
     */
    CANCELLED
}
