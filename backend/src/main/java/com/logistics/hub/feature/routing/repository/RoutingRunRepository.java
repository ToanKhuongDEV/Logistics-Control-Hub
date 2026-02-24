package com.logistics.hub.feature.routing.repository;

import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoutingRunRepository extends JpaRepository<RoutingRunEntity, Long> {

  Long countByStatus(RoutingRunStatus status);

  @Query("SELECT r FROM RoutingRunEntity r LEFT JOIN FETCH r.routes WHERE r.id = :id")
  Optional<RoutingRunEntity> findByIdWithRoutes(@Param("id") Long id);
}
