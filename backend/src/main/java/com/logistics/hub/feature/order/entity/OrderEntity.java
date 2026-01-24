package com.logistics.hub.feature.order.entity;

import com.logistics.hub.feature.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity representing Order
 * Maps to table: orders
 * Loose Coupling: deliveryLocationId (FK -> locations.id)
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Delivery Location ID - Loose coupling
     * Database has FK constraint -> locations.id
     */
    @Column(name = "delivery_location_id", nullable = false)
    private Long deliveryLocationId;

    @Column(name = "weight_kg")
    private Integer weightKg;

    @Column(name = "volume_m3", precision = 6, scale = 2)
    private BigDecimal volumeM3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.CREATED;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
