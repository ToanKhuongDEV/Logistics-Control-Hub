package com.logistics.hub.feature.routing.service;

import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

public interface RoutingService {
    RoutingRunEntity optimizeRoutes(List<OrderEntity> orders, List<VehicleEntity> vehicles,
            List<LocationEntity> locations);

    RoutingRunEntity executeRouting(List<Long> orderIds, List<Long> vehicleIds);

    RoutingRunEntity executeAutoRouting(Long depotId);

    Optional<RoutingRunEntity> getLatestRunByDepot(Long depotId);

    Page<RoutingRunEntity> getHistoryByDepot(Long depotId, int page, int size);
}
