package com.logistics.hub.feature.order.entity;

import com.logistics.hub.common.valueobject.TimeWindow;
import com.logistics.hub.feature.order.enums.DeliveryOrderStatus;
import com.logistics.hub.feature.order.enums.OrderPriority;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Entity representing DeliveryOrder (Delivery Order)
 * Main input for AI route optimization
 * Loose Coupling: customerId, pickupLocationId, deliveryLocationId
 */
@Entity
@Table(name = "delivery_orders",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_order_number", columnNames = "order_number")
    },
    indexes = {
        @Index(name = "idx_order_customer", columnList = "customer_id"),
        @Index(name = "idx_order_pickup_location", columnList = "pickup_location_id"),
        @Index(name = "idx_order_delivery_location", columnList = "delivery_location_id"),
        @Index(name = "idx_order_status", columnList = "status"),
        @Index(name = "idx_order_priority", columnList = "priority")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Order number is required")
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    /**
     * Customer ID - Loose coupling
     * No FK constraint (customer may be in another service)
     */
    @NotNull(message = "Customer ID is required")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * Pickup Location ID - Loose coupling
     * Database has FK constraint -> locations.id
     */
    @NotNull(message = "Pickup location is required")
    @Column(name = "pickup_location_id", nullable = false)
    private Long pickupLocationId;

    /**
     * Delivery Location ID - Loose coupling
     * Database has FK constraint -> locations.id
     */
    @NotNull(message = "Delivery location is required")
    @Column(name = "delivery_location_id", nullable = false)
    private Long deliveryLocationId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "startTime", column = @Column(name = "delivery_start_time")),
        @AttributeOverride(name = "endTime", column = @Column(name = "delivery_end_time"))
    })
    @NotNull(message = "Delivery time window is required")
    private TimeWindow deliveryTimeWindow;

    /**
     * Pickup time window (optional - if null then flexible)
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "startTime", column = @Column(name = "pickup_start_time")),
        @AttributeOverride(name = "endTime", column = @Column(name = "pickup_end_time"))
    })
    private TimeWindow pickupTimeWindow;

    @Min(value = 0, message = "Weight must be greater than or equal to 0")
    @NotNull(message = "Weight is required")
    @Column(nullable = false)
    private Double weight;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderPriority priority = OrderPriority.NORMAL;

    @NotNull(message = "Order status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryOrderStatus status = DeliveryOrderStatus.PENDING;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}

