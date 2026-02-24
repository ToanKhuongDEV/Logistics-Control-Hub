package com.logistics.hub.feature.order.entity;

import com.logistics.hub.feature.driver.entity.DriverEntity;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_location_id", nullable = false)
    private LocationEntity deliveryLocation;

    @Column(name = "weight_kg")
    private Integer weightKg;

    @Column(name = "volume_m3", precision = 6, scale = 2)
    private BigDecimal volumeM3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private DriverEntity driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.CREATED;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
