package com.logistics.hub.feature.driverportal.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.audit.constant.AuditAction;
import com.logistics.hub.feature.audit.constant.AuditResourceType;
import com.logistics.hub.feature.audit.constant.AuditStatus;
import com.logistics.hub.feature.audit.service.AuditActorService;
import com.logistics.hub.feature.audit.service.AuditLogService;
import com.logistics.hub.feature.auth.policy.AuthorizationPolicy;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.driver.entity.DriverEntity;
import com.logistics.hub.feature.driverportal.dto.response.DriverDeliveryOrderResponse;
import com.logistics.hub.feature.driverportal.service.DriverPortalService;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.routing.entity.RouteEntity;
import com.logistics.hub.feature.routing.entity.RouteStopEntity;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.dto.response.RoutingRunResponse;
import com.logistics.hub.feature.routing.enums.RouteStatus;
import com.logistics.hub.feature.routing.mapper.RoutingMapper;
import com.logistics.hub.feature.routing.repository.RouteRepository;
import com.logistics.hub.feature.routing.repository.RouteStopRepository;
import com.logistics.hub.feature.routing.repository.RoutingRunRepository;
import com.logistics.hub.feature.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriverPortalServiceImpl implements DriverPortalService {

    private static final String DRIVER_NOT_LINKED = "Driver account is not linked to a driver profile.";

    private final AuthorizationService authorizationService;
    private final RouteStopRepository routeStopRepository;
    private final RouteRepository routeRepository;
    private final RoutingRunRepository routingRunRepository;
    private final OrderRepository orderRepository;
    private final AuditLogService auditLogService;
    private final AuditActorService auditActorService;

    @Override
    @Transactional(readOnly = true)
    public Page<DriverDeliveryOrderResponse> findMyOrders(Pageable pageable) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_READ);
        DriverEntity driver = requireCurrentDriver();

        return routeStopRepository
                .findDeliveryStopsByDriverIdAndOrderStatus(driver.getId(), OrderStatus.IN_TRANSIT, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverDeliveryOrderResponse findMyOrder(Long orderId) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_READ);
        DriverEntity driver = requireCurrentDriver();

        RouteStopEntity stop = findStopForDriverOrder(driver.getId(), orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for current driver: " + orderId));

        return toResponse(stop);
    }

    @Override
    @Transactional
    public DriverDeliveryOrderResponse completeMyOrder(Long orderId) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_UPDATE);
        DriverEntity driver = requireCurrentDriver();

        RouteStopEntity stop = findStopForDriverOrder(driver.getId(), orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for current driver: " + orderId));

        OrderEntity order = stop.getOrder();
        if (order == null) {
            throw new ResourceNotFoundException("Order not found for current driver: " + orderId);
        }

        if (order.getStatus() != OrderStatus.IN_TRANSIT) {
            throw new ValidationException("Only IN_TRANSIT orders can be completed by the driver.");
        }

        RouteEntity route = stop.getRoute();
        if (route != null && route.getStatus() == RouteStatus.CANCELLED) {
            throw new ValidationException("Cannot complete an order from a cancelled route.");
        }

        Map<String, Object> beforeData = orderSnapshot(order, route);
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        boolean routeAutoCompleted = updateRouteAfterOrderCompletion(route);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("driverId", driver.getId());
        metadata.put("orderId", order.getId());
        metadata.put("routeId", route != null ? route.getId() : null);
        metadata.put("routeAutoCompleted", routeAutoCompleted);

        auditLogService.log(
                auditActorService.getCurrentActor(),
                AuditAction.UPDATE,
                AuditResourceType.ORDER,
                order.getId().toString(),
                order.getCode(),
                order.getDepot() != null ? order.getDepot().getId() : null,
                AuditStatus.SUCCESS,
                "Driver completed delivery order",
                beforeData,
                orderSnapshot(order, route),
                metadata);

        return toResponse(stop);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoutingRunResponse> findMyRoutingHistory(Pageable pageable) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_READ);
        DriverEntity driver = requireCurrentDriver();

        return routingRunRepository.findAllByDriverId(driver.getId(), pageable)
                .map(run -> toDriverScopedRoutingRunResponse(run, driver.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public RoutingRunResponse findMyRoutingRun(Long runId) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_READ);
        DriverEntity driver = requireCurrentDriver();

        RoutingRunEntity run = routingRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Routing run not found: " + runId));

        RoutingRunResponse response = toDriverScopedRoutingRunResponse(run, driver.getId());
        if (response.getRoutes().isEmpty()) {
            throw new ResourceNotFoundException("Routing run not found for current driver: " + runId);
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RoutingRunResponse findMyLatestRoutingRun() {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_DELIVERY_READ);
        DriverEntity driver = requireCurrentDriver();

        return routingRunRepository.findAllByDriverId(driver.getId(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(run -> toDriverScopedRoutingRunResponse(run, driver.getId()))
                .orElse(null);
    }

    private DriverEntity requireCurrentDriver() {
        UserEntity currentUser = authorizationService.getCurrentUser();
        if (currentUser.getDriver() == null) {
            throw new ValidationException(DRIVER_NOT_LINKED);
        }
        return currentUser.getDriver();
    }

    private Optional<RouteStopEntity> findStopForDriverOrder(Long driverId, Long orderId) {
        return routeStopRepository
                .findDeliveryStopsByDriverIdAndOrderId(driverId, orderId, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }

    private boolean updateRouteAfterOrderCompletion(RouteEntity route) {
        if (route == null) {
            return false;
        }

        List<RouteStopEntity> orderStops = routeStopRepository.findOrderStopsByRouteIdWithOrders(route.getId());
        boolean allDelivered = !orderStops.isEmpty()
                && orderStops.stream()
                        .map(RouteStopEntity::getOrder)
                        .allMatch(order -> order != null && order.getStatus() == OrderStatus.DELIVERED);

        route.setStatus(allDelivered ? RouteStatus.COMPLETED : RouteStatus.IN_PROGRESS);
        routeRepository.save(route);
        return allDelivered;
    }

    private RoutingRunResponse toDriverScopedRoutingRunResponse(RoutingRunEntity run, Long driverId) {
        RoutingRunResponse response = new RoutingRunResponse();
        response.setId(run.getId());
        response.setStatus(run.getStatus() != null ? run.getStatus().name() : null);
        response.setStartTime(run.getStartTime());
        response.setEndTime(run.getEndTime());
        response.setTotalDistanceKm(run.getTotalDistanceKm());
        response.setTotalCost(run.getTotalCost());
        response.setConfiguration(run.getConfiguration());
        response.setCreatedAt(run.getCreatedAt());
        response.setRoutes(routeRepository.findAllByRoutingRunIdAndDriverId(run.getId(), driverId)
                .stream()
                .map(RoutingMapper::toRouteResponse)
                .toList());
        return response;
    }

    private DriverDeliveryOrderResponse toResponse(RouteStopEntity stop) {
        OrderEntity order = stop.getOrder();
        LocationEntity location = order != null && order.getDeliveryLocation() != null
                ? order.getDeliveryLocation()
                : stop.getLocation();
        DepotEntity depot = order != null ? order.getDepot() : null;

        DriverDeliveryOrderResponse response = new DriverDeliveryOrderResponse();
        response.setId(order != null ? order.getId() : null);
        response.setCode(order != null ? order.getCode() : null);
        response.setDeliveryLocationName(formatLocation(location));
        response.setDeliveryStreet(location != null ? location.getStreet() : null);
        response.setDeliveryCity(location != null ? location.getCity() : null);
        response.setDeliveryCountry(location != null ? location.getCountry() : null);
        response.setWeightKg(order != null ? order.getWeightKg() : null);
        response.setVolumeM3(order != null ? order.getVolumeM3() : null);
        response.setDepotId(depot != null ? depot.getId() : null);
        response.setDepotName(depot != null ? depot.getName() : null);
        response.setLatitude(location != null ? location.getLatitude() : null);
        response.setLongitude(location != null ? location.getLongitude() : null);
        response.setStatus(order != null ? order.getStatus() : null);
        response.setCreatedAt(order != null ? order.getCreatedAt() : null);
        response.setRouteId(stop.getRoute() != null ? stop.getRoute().getId() : null);
        response.setStopId(stop.getId());
        response.setStopSequence(stop.getStopSequence());
        return response;
    }

    private String formatLocation(LocationEntity location) {
        if (location == null) {
            return null;
        }
        return String.join(", ", List.of(
                safe(location.getStreet()),
                safe(location.getCity()),
                safe(location.getCountry())));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private Map<String, Object> orderSnapshot(OrderEntity order, RouteEntity route) {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", order.getId());
        snapshot.put("code", order.getCode());
        snapshot.put("status", order.getStatus() != null ? order.getStatus().name() : null);
        snapshot.put("driverId", order.getDriver() != null ? order.getDriver().getId() : null);
        snapshot.put("depotId", order.getDepot() != null ? order.getDepot().getId() : null);
        snapshot.put("routeId", route != null ? route.getId() : null);
        snapshot.put("routeStatus", route != null && route.getStatus() != null ? route.getStatus().name() : null);
        return snapshot;
    }
}
