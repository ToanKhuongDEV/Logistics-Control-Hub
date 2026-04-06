package com.logistics.hub.feature.auth.service;

import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.user.entity.UserEntity;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;

import java.util.Set;

public interface AuthorizationService {

    UserEntity getCurrentUser();

    boolean hasPermission(String permission);

    void requirePermission(String permission);

    Set<String> getCurrentPermissions();

    Set<Long> getAccessibleDepotIds();

    boolean hasGlobalScope();

    void requireDepotAccess(Long depotId);

    void requireOrderAccess(OrderEntity order);

    void requireVehicleAccess(VehicleEntity vehicle);
}
