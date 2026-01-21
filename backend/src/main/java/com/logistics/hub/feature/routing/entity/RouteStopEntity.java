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
 * Entity đại diện cho RouteStop (Điểm dừng trên tuyến)
 * So sánh planned vs actual
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
     * Database có FK constraint → route_plans.id
     */
    @NotNull(message = "Route Plan ID không được để trống")
    @Column(name = "route_plan_id", nullable = false)
    private Long routePlanId;

    /**
     * Location ID - Loose coupling
     * Database có FK constraint → locations.id
     */
    @NotNull(message = "Location ID không được để trống")
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    /**
     * Delivery Order ID - Loose coupling (nullable - depot stop không có order)
     * Database có FK constraint → delivery_orders.id
     */
    @Column(name = "delivery_order_id")
    private Long deliveryOrderId;

    /**
     * Stop Type - Phân loại điểm dừng
     */
    @NotNull(message = "Stop type không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "stop_type", nullable = false, length = 20)
    private StopType stopType;

    @Min(value = 1, message = "Thứ tự phải >= 1")
    @NotNull(message = "Thứ tự không được để trống")
    @Column(nullable = false)
    private Integer sequence;

    @NotNull(message = "Thời gian đến dự kiến không được để trống")
    @Column(name = "planned_arrival", nullable = false)
    private Instant plannedArrival;

    @NotNull(message = "Thời gian rời dự kiến không được để trống")
    @Column(name = "planned_departure", nullable = false)
    private Instant plannedDeparture;

    @Column(name = "actual_arrival")
    private Instant actualArrival;

    @Column(name = "actual_departure")
    private Instant actualDeparture;

    /**
     * Khoảng cách từ điểm dừng trước (km)
     */
    @Column(name = "distance_from_prev_km")
    private Double distanceFromPrevKm;

    /**
     * Thời gian di chuyển từ điểm dừng trước (phút)
     */
    @Column(name = "duration_from_prev_minutes")
    private Integer durationFromPrevMinutes;

    /**
     * Thời gian xử lý tại điểm dừng (phút)
     */
    @Column(name = "service_time_minutes")
    private Integer serviceTimeMinutes = 15;

    @NotNull(message = "Status không được để trống")
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
