package com.logistics.hub.feature.event.enums;

/**
 * Trạng thái sự cố
 */
public enum DisruptionStatus {
    /**
     * Đã phát hiện
     */
    DETECTED,

    /**
     * Đang xử lý
     */
    IN_PROGRESS,

    /**
     * Đã giải quyết
     */
    RESOLVED,

    /**
     * Bỏ qua
     */
    IGNORED
}
