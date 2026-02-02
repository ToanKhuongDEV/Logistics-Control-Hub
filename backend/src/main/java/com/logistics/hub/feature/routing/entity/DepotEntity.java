package com.logistics.hub.feature.routing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;


@Entity
@Table(name = "depots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
