package com.logistics.hub.feature.dashboard.service.impl;

import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.dashboard.dto.response.DashboardStatisticsResponse;
import com.logistics.hub.feature.dashboard.service.DashboardService;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.driver.repository.DriverRepository;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.redis.constant.CacheConstant;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import com.logistics.hub.feature.routing.repository.RoutingRunRepository;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

  private final VehicleRepository vehicleRepository;
  private final OrderRepository orderRepository;
  private final DepotRepository depotRepository;
  private final DriverRepository driverRepository;
  private final RoutingRunRepository routingRunRepository;
  private final AuthorizationService authorizationService;

  @Override
  public DashboardStatisticsResponse getStatistics(Long depotId) {
    log.info("Fetching dashboard statistics for depotId: {}", depotId);

    if (!authorizationService.isAdmin() && depotId != null) {
      authorizationService.requireDepotAccess(depotId);
    }

    Long activeVehicles;
    Long totalOrders;
    Long ordersCreated;
    Long ordersInTransit;
    Long ordersDelivered;
    Long ordersCancelled;

    if (depotId != null) {
      activeVehicles = vehicleRepository.countByStatusAndDepot_Id(VehicleStatus.ACTIVE, depotId);
      totalOrders = orderRepository.countByDepot_Id(depotId);
      ordersCreated = orderRepository.countByStatusAndDepot_Id(OrderStatus.CREATED, depotId);
      ordersInTransit = orderRepository.countByStatusAndDepot_Id(OrderStatus.IN_TRANSIT, depotId);
      ordersDelivered = orderRepository.countByStatusAndDepot_Id(OrderStatus.DELIVERED, depotId);
      ordersCancelled = orderRepository.countByStatusAndDepot_Id(OrderStatus.CANCELLED, depotId);
    } else if (authorizationService.isAdmin()) {
      activeVehicles = vehicleRepository.countByStatus(VehicleStatus.ACTIVE);
      totalOrders = orderRepository.count();
      ordersCreated = orderRepository.countByStatus(OrderStatus.CREATED);
      ordersInTransit = orderRepository.countByStatus(OrderStatus.IN_TRANSIT);
      ordersDelivered = orderRepository.countByStatus(OrderStatus.DELIVERED);
      ordersCancelled = orderRepository.countByStatus(OrderStatus.CANCELLED);
    } else {
      Set<Long> accessibleDepotIds = authorizationService.getAccessibleDepotIds();
      activeVehicles = accessibleDepotIds.stream()
          .map(id -> vehicleRepository.countByStatusAndDepot_Id(VehicleStatus.ACTIVE, id))
          .reduce(0L, Long::sum);
      totalOrders = orderRepository.countByDepot_IdIn(accessibleDepotIds);
      ordersCreated = accessibleDepotIds.stream()
          .map(id -> orderRepository.countByStatusAndDepot_Id(OrderStatus.CREATED, id))
          .reduce(0L, Long::sum);
      ordersInTransit = accessibleDepotIds.stream()
          .map(id -> orderRepository.countByStatusAndDepot_Id(OrderStatus.IN_TRANSIT, id))
          .reduce(0L, Long::sum);
      ordersDelivered = accessibleDepotIds.stream()
          .map(id -> orderRepository.countByStatusAndDepot_Id(OrderStatus.DELIVERED, id))
          .reduce(0L, Long::sum);
      ordersCancelled = accessibleDepotIds.stream()
          .map(id -> orderRepository.countByStatusAndDepot_Id(OrderStatus.CANCELLED, id))
          .reduce(0L, Long::sum);
    }

    Long activeDepots = authorizationService.isAdmin()
        ? depotRepository.countByIsActive(true)
        : depotRepository.countByIsActiveAndIdIn(true, authorizationService.getAccessibleDepotIds());
    Long totalDrivers = authorizationService.isAdmin()
        ? driverRepository.count()
        : driverRepository.countDistinctByDepotIds(authorizationService.getAccessibleDepotIds());
    Long totalRoutingRuns = depotId != null ? routingRunRepository.countByDepot_Id(depotId)
        : authorizationService.isAdmin()
            ? routingRunRepository.count()
            : authorizationService.getAccessibleDepotIds().stream()
                .map(routingRunRepository::countByDepot_Id)
                .reduce(0L, Long::sum);
    Long successfulRuns = depotId != null
        ? routingRunRepository.countByStatusAndDepot_Id(RoutingRunStatus.COMPLETED, depotId)
        : authorizationService.isAdmin()
            ? routingRunRepository.countByStatus(RoutingRunStatus.COMPLETED)
            : authorizationService.getAccessibleDepotIds().stream()
                .map(id -> routingRunRepository.countByStatusAndDepot_Id(RoutingRunStatus.COMPLETED, id))
                .reduce(0L, Long::sum);

    return DashboardStatisticsResponse.builder()
        .activeVehicles(activeVehicles)
        .totalOrders(totalOrders)
        .ordersCreated(ordersCreated)
        .ordersInTransit(ordersInTransit)
        .ordersDelivered(ordersDelivered)
        .ordersCancelled(ordersCancelled)
        .activeDepots(activeDepots)
        .totalDrivers(totalDrivers)
        .totalRoutingRuns(totalRoutingRuns)
        .successfulRuns(successfulRuns)
        .build();
  }
}
