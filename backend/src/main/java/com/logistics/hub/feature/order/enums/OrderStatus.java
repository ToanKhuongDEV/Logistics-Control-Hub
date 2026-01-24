package com.logistics.hub.feature.order.enums;

/**
 * Order status enum
 * Matches database: status VARCHAR(30) DEFAULT 'CREATED'
 */
public enum OrderStatus {
    CREATED,
    ASSIGNED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
