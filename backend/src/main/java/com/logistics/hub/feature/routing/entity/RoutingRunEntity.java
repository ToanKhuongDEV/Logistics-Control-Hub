package com.logistics.hub.feature.routing.entity;

import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "routing_runs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoutingRunStatus status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "total_distance_km", precision = 12, scale = 2)
    private BigDecimal totalDistanceKm;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(columnDefinition = "TEXT")
    private String configuration;

    @OneToMany(mappedBy = "routingRun", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RouteEntity> routes;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
