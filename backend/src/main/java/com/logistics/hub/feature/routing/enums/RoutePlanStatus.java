package com.logistics.hub.feature.routing.enums;

/**
 * Route plan status enumeration
 */
public enum RoutePlanStatus {
    /**
     * Newly created plan
     */
    DRAFT,

    /**
     * Active
     */
    ACTIVE,

    /**
     * Completed
     */
    COMPLETED,

    /**
     * Cancelled/Superseded
     */
    SUPERSEDED,

    /**
     * Failed
     */
    FAILED
}

