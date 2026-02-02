package com.logistics.hub.feature.routing.service;

import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;

import java.util.List;

public interface RoutingService {
    RoutingRunEntity optimizeRoutes(List<OrderEntity> orders, List<VehicleEntity> vehicles);
}
