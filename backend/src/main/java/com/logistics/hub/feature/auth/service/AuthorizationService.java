package com.logistics.hub.feature.auth.service;

import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.user.entity.UserEntity;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;

import java.util.Set;

public interface AuthorizationService {

    String ROLE_ADMIN = "ADMIN";
    String ROLE_DISPATCHER = "DISPATCHER";

    UserEntity getCurrentUser();

    boolean isAdmin();

    boolean isDispatcher();

    Set<Long> getAccessibleDepotIds();

    void requireAdmin();

    void requireDepotAccess(Long depotId);

    void requireOrderAccess(OrderEntity order);

    void requireVehicleAccess(VehicleEntity vehicle);
}
