package com.logistics.hub.feature.depot.entity;

import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "depots")
@Getter
@Setter
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dispatcher_id")
  private UserEntity dispatcher;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof DepotEntity other))
      return false;
    return id != null && id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
