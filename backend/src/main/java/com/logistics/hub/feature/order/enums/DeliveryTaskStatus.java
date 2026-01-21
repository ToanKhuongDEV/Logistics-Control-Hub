package com.logistics.hub.feature.order.enums;

/**
 * Delivery task execution status enumeration
 */
public enum DeliveryTaskStatus {
    /**
     * Task newly created, not started
     */
    CREATED,

    /**
     * Picking up in progress
     */
    PICKING_UP,

    /**
     * Picked up, in transit
     */
    IN_TRANSIT,

    /**
     * Delivering to customer
     */
    DELIVERING,

    /**
     * Delivery completed
     */
    COMPLETED,

    /**
     * Failed
     */
    FAILED,

    /**
     * Cancelled
     */
    CANCELLED
}

