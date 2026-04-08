package com.logistics.hub.feature.auth.service.impl;

import com.logistics.hub.common.exception.ForbiddenException;
import com.logistics.hub.common.exception.UnauthorizedException;
import com.logistics.hub.feature.auth.constant.AuthConstant;
import com.logistics.hub.feature.auth.policy.AuthorizationPolicy;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.driver.entity.DriverEntity;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.user.entity.UserEntity;
import com.logistics.hub.feature.user.repository.UserRepository;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(AuthConstant.NOT_AUTHENTICATED);
        }

        return userRepository.findByUsernameWithAssignedDepots(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException(AuthConstant.NOT_AUTHENTICATED));
    }

    @Override
    public boolean hasPermission(String permission) {
        return getCurrentPermissions().contains(permission);
    }

    @Override
    public void requirePermission(String permission) {
        if (!hasPermission(permission)) {
            throw new ForbiddenException("Bạn không có quyền thực hiện thao tác này.");
        }
    }

    @Override
    public Set<String> getCurrentPermissions() {
        return AuthorizationPolicy.permissionsForRole(getCurrentUser().getRole());
    }

    @Override
    public Set<Long> getAccessibleDepotIds() {
        UserEntity user = getCurrentUser();
        if (AuthorizationPolicy.hasGlobalScope(user.getRole())) {
            return Set.of();
        }

        return user.getAssignedDepots().stream()
                .map(assignedDepot -> assignedDepot.getId())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean hasGlobalScope() {
        return AuthorizationPolicy.hasGlobalScope(getCurrentUser().getRole());
    }

    @Override
    public void requireDepotAccess(Long depotId) {
        if (depotId == null || hasGlobalScope()) {
            return;
        }

        if (!getAccessibleDepotIds().contains(depotId)) {
            throw new ForbiddenException("Bạn không có quyền truy cập kho này.");
        }
    }

    @Override
    public void requireOrderAccess(OrderEntity order) {
        if (order == null || order.getDepot() == null) {
            if (!hasGlobalScope()) {
                throw new ForbiddenException("Bạn không có quyền truy cập đơn hàng này.");
            }
            return;
        }

        requireDepotAccess(order.getDepot().getId());
    }

    @Override
    public void requireDriverAccess(DriverEntity driver) {
        if (driver == null) {
            if (!hasGlobalScope()) {
                throw new ForbiddenException("Ban khong co quyen truy cap tai xe nay.");
            }
            return;
        }

        if (hasGlobalScope()) {
            return;
        }

        Set<Long> accessibleDepotIds = getAccessibleDepotIds();
        if (accessibleDepotIds.isEmpty()
                || !vehicleRepository.existsByDriver_IdAndDepot_IdIn(driver.getId(), accessibleDepotIds)) {
            throw new ForbiddenException("Ban khong co quyen truy cap tai xe nay.");
        }
    }

    @Override
    public void requireVehicleAccess(VehicleEntity vehicle) {
        if (vehicle == null || vehicle.getDepot() == null) {
            if (!hasGlobalScope()) {
                throw new ForbiddenException("Bạn không có quyền truy cập phương tiện này.");
            }
            return;
        }

        requireDepotAccess(vehicle.getDepot().getId());
    }
}
