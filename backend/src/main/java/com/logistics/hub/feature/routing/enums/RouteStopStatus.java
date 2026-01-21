package com.logistics.hub.feature.routing.enums;

/**
 * Route stop status enumeration
 */
public enum RouteStopStatus {
    /**
     * Not started
     */
    PENDING,

    /**
     * On the way
     */
    EN_ROUTE,

    /**
     * Arrived
     */
    ARRIVED,

    /**
     * Completed
     */
    COMPLETED,

    /**
     * Skipped
     */
    SKIPPED,

    /**
     * Failed
     */
    FAILED
}

