package com.logistics.hub.feature.order.entity;

import com.logistics.hub.feature.order.enums.DeliveryTaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing DeliveryTask (Delivery Execution)
 * Separates plan vs execution
 * Loose Coupling: deliveryOrderId, vehicleId, driverId, routePlanId
 */
@Entity
@Table(name = "delivery_tasks",
    indexes = {
        @Index(name = "idx_task_order", columnList = "delivery_order_id"),
        @Index(name = "idx_task_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_task_driver", columnList = "driver_id"),
        @Index(name = "idx_task_route", columnList = "route_plan_id"),
        @Index(name = "idx_task_status", columnList = "status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Delivery Order ID - Loose coupling
     * Database has FK constraint -> delivery_orders.id
     */
    @NotNull(message = "Delivery Order ID is required")
    @Column(name = "delivery_order_id", nullable = false)
    private Long deliveryOrderId;

    /**
     * Vehicle ID - Loose coupling
     * Database has FK constraint -> vehicles.id
     */
    @NotNull(message = "Vehicle ID is required")
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    /**
     * Driver ID - Loose coupling
     * Database has FK constraint -> drivers.id
     */
    @Column(name = "driver_id")
    private Long driverId;

    /**
     * Route Plan ID - Loose coupling
     * Database has FK constraint -> route_plans.id
     */
    @Column(name = "route_plan_id")
    private Long routePlanId;

    @NotNull(message = "Task status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryTaskStatus status = DeliveryTaskStatus.CREATED;

    @Column(name = "actual_pickup_time")
    private Instant actualPickupTime;

    @Column(name = "actual_delivery_time")
    private Instant actualDeliveryTime;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}

