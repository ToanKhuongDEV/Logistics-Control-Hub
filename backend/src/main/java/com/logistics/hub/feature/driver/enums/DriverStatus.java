package com.logistics.hub.feature.driver.enums;

/**
 * Trạng thái của tài xế
 */
public enum DriverStatus {
    /**
     * Tài xế sẵn sàng nhận việc
     */
    AVAILABLE,

    /**
     * Tài xế đang lái xe giao hàng
     */
    DRIVING,

    /**
     * Tài xế nghỉ phép/không làm việc
     */
    OFF_DUTY
}
