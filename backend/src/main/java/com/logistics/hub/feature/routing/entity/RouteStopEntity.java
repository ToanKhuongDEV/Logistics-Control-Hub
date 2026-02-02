package com.logistics.hub.feature.routing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;


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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteEntity route;


    @Column(name = "location_id", nullable = false)
    private Long locationId;


    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "distance_from_prev_km", precision = 10, scale = 2)
    private BigDecimal distanceFromPrevKm;

    @Column(name = "duration_from_prev_min")
    private Integer durationFromPrevMin;

    @Column(name = "arrival_time")
    private Instant arrivalTime;

    @Column(name = "departure_time")
    private Instant departureTime;
}
