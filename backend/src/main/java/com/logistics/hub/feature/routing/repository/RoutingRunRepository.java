package com.logistics.hub.feature.routing.repository;

import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoutingRunRepository extends JpaRepository<RoutingRunEntity, Long> {

  Long countByStatus(RoutingRunStatus status);

  Long countByDepot_Id(Long depotId);

  Long countByStatusAndDepot_Id(RoutingRunStatus status, Long depotId);

  @Query("SELECT r FROM RoutingRunEntity r LEFT JOIN FETCH r.routes WHERE r.id = :id")
  Optional<RoutingRunEntity> findByIdWithRoutes(@Param("id") Long id);

  @Query("SELECT r FROM RoutingRunEntity r " +
      "LEFT JOIN FETCH r.routes " +
      "WHERE r.depot.id = :depotId AND r.status = :status " +
      "ORDER BY r.createdAt DESC")
  List<RoutingRunEntity> findLatestByDepot_Id(@Param("depotId") Long depotId, @Param("status") RoutingRunStatus status);
}
