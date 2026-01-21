package com.logistics.hub.feature.control.enums;

/**
 * Loại can thiệp thủ công
 */
public enum OverrideType {
    /**
     * Thay đổi thủ công route plan
     */
    ROUTE_MODIFICATION,

    /**
     * Thay đổi vehicle assignment
     */
    VEHICLE_CHANGE,

    /**
     * Thay đổi độ ưu tiên đơn hàng
     */
    PRIORITY_CHANGE,

    /**
     * Vô hiệu hóa vehicle
     */
    VEHICLE_DISABLE,

    /**
     * Khóa route (không cho AI thay đổi)
     */
    ROUTE_LOCK,

    /**
     * Hủy optimization
     */
    OPTIMIZATION_CANCEL,

    /**
     * Khác
     */
    OTHER
}
