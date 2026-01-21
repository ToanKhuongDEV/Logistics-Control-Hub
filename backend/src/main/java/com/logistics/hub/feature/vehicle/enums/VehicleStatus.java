package com.logistics.hub.feature.vehicle.enums;

/**
 * Trạng thái của xe giao hàng
 */
public enum VehicleStatus {
    /**
     * Xe sẵn sàng để sử dụng
     */
    AVAILABLE,

    /**
     * Xe đang được sử dụng để giao hàng
     */
    IN_USE,

    /**
     * Xe đang bảo trì
     */
    MAINTENANCE,

    /**
     * Xe bị vô hiệu hóa (không thể sử dụng)
     */
    DISABLED
}
