package com.logistics.hub.feature.routing.enums;

/**
 * Trạng thái chạy tối ưu
 */
public enum OptimizationStatus {
    /**
     * Đang chờ xử lý
     */
    PENDING,

    /**
     * Đang chạy
     */
    RUNNING,

    /**
     * Hoàn thành thành công
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
