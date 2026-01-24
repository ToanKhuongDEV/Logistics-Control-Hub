package com.logistics.hub.feature.routing.enums;

/**
 * Route status enum
 * Matches database: status VARCHAR(30) DEFAULT 'CREATED'
 */
public enum RouteStatus {
    CREATED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
