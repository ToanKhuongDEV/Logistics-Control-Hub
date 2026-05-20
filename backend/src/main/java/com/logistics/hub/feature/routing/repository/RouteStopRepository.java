package com.logistics.hub.feature.routing.repository;

import com.logistics.hub.feature.routing.entity.RouteStopEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStopEntity, Long> {

  boolean existsByOrderId(Long orderId);

  @EntityGraph(attributePaths = {"route", "order", "order.deliveryLocation", "order.depot", "location"})
  @Query(value = """
      SELECT rs
      FROM RouteStopEntity rs
      JOIN rs.route r
      JOIN rs.order o
      WHERE o.driver.id = :driverId
        AND o.status = :status
      ORDER BY r.id ASC, rs.stopSequence ASC
      """,
      countQuery = """
      SELECT COUNT(rs)
      FROM RouteStopEntity rs
      JOIN rs.order o
      WHERE o.driver.id = :driverId
        AND o.status = :status
      """)
  Page<RouteStopEntity> findDeliveryStopsByDriverIdAndOrderStatus(
      @Param("driverId") Long driverId,
      @Param("status") OrderStatus status,
      Pageable pageable);

  @EntityGraph(attributePaths = {"route", "order", "order.deliveryLocation", "order.depot", "location"})
  @Query(value = """
      SELECT rs
      FROM RouteStopEntity rs
      JOIN rs.route r
      JOIN rs.order o
      WHERE o.driver.id = :driverId
        AND o.id = :orderId
      ORDER BY r.createdAt DESC, rs.stopSequence ASC
      """,
      countQuery = """
      SELECT COUNT(rs)
      FROM RouteStopEntity rs
      JOIN rs.order o
      WHERE o.driver.id = :driverId
        AND o.id = :orderId
      """)
  Page<RouteStopEntity> findDeliveryStopsByDriverIdAndOrderId(
      @Param("driverId") Long driverId,
      @Param("orderId") Long orderId,
      Pageable pageable);

  @EntityGraph(attributePaths = {"order"})
  @Query("""
      SELECT rs
      FROM RouteStopEntity rs
      WHERE rs.route.id = :routeId
        AND rs.order IS NOT NULL
      ORDER BY rs.stopSequence ASC
      """)
  List<RouteStopEntity> findOrderStopsByRouteIdWithOrders(@Param("routeId") Long routeId);
}
