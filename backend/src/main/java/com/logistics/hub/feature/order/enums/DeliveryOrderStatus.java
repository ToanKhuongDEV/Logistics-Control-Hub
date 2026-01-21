package com.logistics.hub.feature.order.enums;

/**
 * Delivery order status enumeration
 */
public enum DeliveryOrderStatus {
    /**
     * New order, not yet planned
     */
    PENDING,

    /**
     * AI optimized and planned
     */
    PLANNED,

    /**
     * Vehicle and driver assigned
     */
    ASSIGNED,

    /**
     * In transit
     */
    IN_TRANSIT,

    /**
     * Successfully delivered
     */
    DELIVERED,

    /**
     * Delivery failed
     */
    FAILED,

    /**
     * Order cancelled
     */
    CANCELLED
}

