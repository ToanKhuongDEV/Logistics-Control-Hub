package com.logistics.hub.feature.vehicle.enums;

/**
 * Vehicle status enumeration
 */
public enum VehicleStatus {
    /**
     * Vehicle is available for use
     */
    AVAILABLE,

    /**
     * Vehicle is currently in use for delivery
     */
    IN_USE,

    /**
     * Vehicle is under maintenance
     */
    MAINTENANCE,

    /**
     * Vehicle is disabled (cannot be used)
     */
    DISABLED
}

