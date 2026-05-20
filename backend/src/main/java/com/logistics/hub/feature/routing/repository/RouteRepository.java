package com.logistics.hub.feature.routing.repository;

import com.logistics.hub.feature.routing.entity.RouteEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, Long> {

  boolean existsByVehicleId(Long vehicleId);

  @EntityGraph(attributePaths = {"vehicle", "stops", "stops.location", "stops.order"})
  @Query("""
      SELECT DISTINCT r
      FROM RouteEntity r
      JOIN r.vehicle v
      WHERE r.routingRun.id = :runId
        AND v.driver.id = :driverId
      ORDER BY r.id ASC
      """)
  List<RouteEntity> findAllByRoutingRunIdAndDriverId(
      @Param("runId") Long runId,
      @Param("driverId") Long driverId);
}
