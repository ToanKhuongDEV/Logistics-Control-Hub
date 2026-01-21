package com.logistics.hub.feature.order.enums;

/**
 * Mức độ ưu tiên đơn hàng
 */
public enum OrderPriority {
    /**
     * Ưu tiên thấp
     */
    LOW,

    /**
     * Ưu tiên thường (mặc định)
     */
    NORMAL,

    /**
     * Ưu tiên cao
     */
    HIGH,

    /**
     * Khẩn cấp - ưu tiên tối đa
     */
    URGENT
}
