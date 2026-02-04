package com.logistics.hub.feature.depot.repository;

import com.logistics.hub.feature.depot.entity.DepotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepotRepository extends JpaRepository<DepotEntity, Long> {
  boolean existsByLocationId(Long locationId);

  boolean existsByLocationIdAndIdNot(Long locationId, Long id);
}
