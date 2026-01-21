package com.logistics.hub.feature.event.enums;

/**
 * Loại sự kiện hệ thống
 */
public enum SystemEventType {
    /**
     * Sự kiện liên quan đến đơn hàng
     */
    ORDER_CREATED,
    ORDER_UPDATED,
    ORDER_CANCELLED,

    /**
     * Sự kiện tối ưu hóa
     */
    OPTIMIZATION_STARTED,
    OPTIMIZATION_COMPLETED,
    OPTIMIZATION_FAILED,

    /**
     * Sự kiện route
     */
    ROUTE_ASSIGNED,
    ROUTE_STARTED,
    ROUTE_COMPLETED,

    /**
     * Sự kiện vehicle
     */
    VEHICLE_DEPARTED,
    VEHICLE_ARRIVED,
    VEHICLE_STATUS_CHANGED,

    /**
     * Sự kiện disruption
     */
    DISRUPTION_DETECTED,
    DISRUPTION_RESOLVED,

    /**
     * Sự kiện khác
     */
    SYSTEM_ALERT,
    SYSTEM_ERROR
}
