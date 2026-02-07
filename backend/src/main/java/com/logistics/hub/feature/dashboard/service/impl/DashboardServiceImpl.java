package com.logistics.hub.feature.dashboard.service.impl;

import com.logistics.hub.feature.dashboard.dto.response.DashboardStatisticsResponse;
import com.logistics.hub.feature.dashboard.service.DashboardService;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.driver.repository.DriverRepository;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import com.logistics.hub.feature.routing.repository.RoutingRunRepository;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

  private final VehicleRepository vehicleRepository;
  private final OrderRepository orderRepository;
  private final DepotRepository depotRepository;
  private final DriverRepository driverRepository;
  private final RoutingRunRepository routingRunRepository;

  @Override
  public DashboardStatisticsResponse getStatistics() {
    log.info("Fetching dashboard statistics");

    Long activeVehicles = vehicleRepository.countByStatus(VehicleStatus.ACTIVE);
    Long totalOrders = orderRepository.count();
    Long ordersCreated = orderRepository.countByStatus(OrderStatus.CREATED);
    Long ordersInTransit = orderRepository.countByStatus(OrderStatus.IN_TRANSIT);
    Long ordersDelivered = orderRepository.countByStatus(OrderStatus.DELIVERED);
    Long ordersCancelled = orderRepository.countByStatus(OrderStatus.CANCELLED);
    Long activeDepots = depotRepository.countByIsActive(true);
    Long totalDrivers = driverRepository.count();
    Long totalRoutingRuns = routingRunRepository.count();
    Long successfulRuns = routingRunRepository.countByStatus(RoutingRunStatus.COMPLETED);

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
