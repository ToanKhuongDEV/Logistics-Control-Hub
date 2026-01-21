package com.logistics.hub.feature.order.enums;

/**
 * Trạng thái đơn hàng giao nhận
 */
public enum DeliveryOrderStatus {
    /**
     * Đơn hàng mới tạo, chưa được lên kế hoạch
     */
    PENDING,

    /**
     * Đã được AI tối ưu và lên kế hoạch
     */
    PLANNED,

    /**
     * Đã gán xe và tài xế
     */
    ASSIGNED,

    /**
     * Đang giao hàng
     */
    IN_TRANSIT,

    /**
     * Đã giao thành công
     */
    DELIVERED,

    /**
     * Giao thất bại
     */
    FAILED,

    /**
     * Đơn hàng bị hủy
     */
    CANCELLED
}
