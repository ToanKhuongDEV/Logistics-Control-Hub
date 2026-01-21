package com.logistics.hub.feature.routing.enums;

/**
 * Optimization trigger type enumeration
 */
public enum OptimizationTriggerType {
    /**
     * Scheduled optimization (daily/hourly)
     */
    SCHEDULED,

    /**
     * Optimization due to disruption
     */
    DISRUPTION,

    /**
     * Manual intervention
     */
    MANUAL,

    /**
     * New orders
     */
    NEW_ORDERS
}

