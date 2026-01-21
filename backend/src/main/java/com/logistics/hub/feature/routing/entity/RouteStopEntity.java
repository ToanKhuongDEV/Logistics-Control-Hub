package com.logistics.hub.feature.routing.entity;

import com.logistics.hub.feature.routing.enums.RouteStopStatus;
import com.logistics.hub.feature.routing.enums.StopType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing RouteStop (Stop on route)
 * Compares planned vs actual
 * Loose Coupling: routePlanId, locationId, deliveryOrderId
 */
@Entity
@Table(name = "route_stops",
    indexes = {
        @Index(name = "idx_stop_route_seq", columnList = "route_plan_id, sequence"),
        @Index(name = "idx_stop_location", columnList = "location_id"),
        @Index(name = "idx_stop_order", columnList = "delivery_order_id"),
        @Index(name = "idx_stop_status", columnList = "status"),
        @Index(name = "idx_stop_type", columnList = "stop_type")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Route Plan ID - Loose coupling
     * Database has FK constraint -> route_plans.id
     */
    @NotNull(message = "Route Plan ID is required")
    @Column(name = "route_plan_id", nullable = false)
    private Long routePlanId;

    /**
     * Location ID - Loose coupling
     * Database has FK constraint -> locations.id
     */
    @NotNull(message = "Location ID is required")
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /**
     * Delivery Order ID - Loose coupling (nullable - depot stop has no order)
     * Database has FK constraint -> delivery_orders.id
     */
    @Column(name = "delivery_order_id")
    private Long deliveryOrderId;

    /**
     * Stop Type - Classifies the stop
     */
    @NotNull(message = "Stop type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "stop_type", nullable = false, length = 20)
    private StopType stopType;

    @Min(value = 1, message = "Sequence must be >= 1")
    @NotNull(message = "Sequence is required")
    @Column(nullable = false)
    private Integer sequence;

    @NotNull(message = "Planned arrival time is required")
    @Column(name = "planned_arrival", nullable = false)
    private Instant plannedArrival;

    @NotNull(message = "Planned departure time is required")
    @Column(name = "planned_departure", nullable = false)
    private Instant plannedDeparture;

    @Column(name = "actual_arrival")
    private Instant actualArrival;

    @Column(name = "actual_departure")
    private Instant actualDeparture;

    /**
     * Distance from previous stop (km)
     */
    @Column(name = "distance_from_prev_km")
    private Double distanceFromPrevKm;

    /**
     * Travel time from previous stop (minutes)
     */
    @Column(name = "duration_from_prev_minutes")
    private Integer durationFromPrevMinutes;

    /**
     * Service time at stop (minutes)
     */
    @Column(name = "service_time_minutes")
    private Integer serviceTimeMinutes = 15;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteStopStatus status = RouteStopStatus.PENDING;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}

