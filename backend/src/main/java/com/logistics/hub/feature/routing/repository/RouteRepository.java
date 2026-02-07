package com.logistics.hub.feature.routing.repository;

import com.logistics.hub.feature.routing.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

  boolean existsByVehicleId(Long vehicleId);
}
