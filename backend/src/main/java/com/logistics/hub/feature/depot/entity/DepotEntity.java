package com.logistics.hub.feature.depot.entity;

import com.logistics.hub.common.base.BaseEntity;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;

@Entity
@Table(name = "depots")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepotEntity extends BaseEntity {

  @Column(nullable = false, length = 255)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id", nullable = false)
  private LocationEntity location;

  @Column(length = 500)
  private String description;

  @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
  private Boolean isActive = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dispatcher_id")
  private UserEntity dispatcher;

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
