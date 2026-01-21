package com.logistics.hub.feature.event.enums;

/**
 * System event type enumeration
 */
public enum SystemEventType {
    /**
     * Order-related events
     */
    ORDER_CREATED,
    ORDER_UPDATED,
    ORDER_CANCELLED,

    /**
     * Optimization events
     */
    OPTIMIZATION_STARTED,
    OPTIMIZATION_COMPLETED,
    OPTIMIZATION_FAILED,

    /**
     * Route events
     */
    ROUTE_ASSIGNED,
    ROUTE_STARTED,
    ROUTE_COMPLETED,

    /**
     * Vehicle events
     */
    VEHICLE_DEPARTED,
    VEHICLE_ARRIVED,
    VEHICLE_STATUS_CHANGED,

    /**
     * Disruption events
     */
    DISRUPTION_DETECTED,
    DISRUPTION_RESOLVED,

    /**
     * Other events
     */
    SYSTEM_ALERT,
    SYSTEM_ERROR
}

