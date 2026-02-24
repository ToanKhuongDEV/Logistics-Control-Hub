package com.logistics.hub.feature.depot.entity;

import com.logistics.hub.feature.location.entity.LocationEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id", nullable = false, unique = true)
  private LocationEntity location;

  @Column(length = 500)
  private String description;

  @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
