package com.logistics.hub.feature.routing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing RouteStop (Ordered delivery sequence)
 * Maps to table: route_stops
 * Loose Coupling: routeId (FK -> routes.id), orderId (FK -> orders.id)
 */
@Entity
@Table(name = "route_stops", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"route_id", "stop_sequence"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Route ID - Loose coupling
     * Database has FK constraint -> routes.id ON DELETE CASCADE
     */
    @Column(name = "route_id", nullable = false)
    private Long routeId;

    /**
     * Location ID - Loose coupling
     * Database has FK constraint -> locations.id
     */
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /**
     * Order ID - Loose coupling (Nullable for depot stops)
     * Database has FK constraint -> orders.id
     */
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "distance_from_prev_km", precision = 10, scale = 2)
    private BigDecimal distanceFromPrevKm;

    @Column(name = "duration_from_prev_min")
    private Integer durationFromPrevMin;
}
