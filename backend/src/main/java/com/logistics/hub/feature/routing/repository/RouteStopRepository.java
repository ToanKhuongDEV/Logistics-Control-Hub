package com.logistics.hub.feature.routing.repository;

import com.logistics.hub.feature.routing.entity.RouteStopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStopEntity, Long> {

  boolean existsByOrderId(Long orderId);
}
