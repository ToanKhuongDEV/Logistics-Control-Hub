package com.logistics.hub.feature.event.enums;

/**
 * Loại sự cố vận hành
 */
public enum DisruptionType {
    /**
     * Tắc đường
     */
    TRAFFIC_JAM,

    /**
     * Xe hỏng
     */
    VEHICLE_BREAKDOWN,

    /**
     * Thời tiết xấu
     */
    WEATHER,

    /**
     * Tai nạn
     */
    ACCIDENT,

    /**
     * Tài xế không khả dụng
     */
    DRIVER_UNAVAILABLE,

    /**
     * Đơn hàng thay đổi
     */
    ORDER_CHANGE,

    /**
     * Khác
     */
    OTHER
}
