package com.logistics.hub.feature.order.service.impl;

import com.logistics.hub.common.exception.ForbiddenException;
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
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.location.service.LocationService;
import com.logistics.hub.feature.order.constant.OrderConstant;
import com.logistics.hub.feature.order.dto.request.OrderRequest;
import com.logistics.hub.feature.order.dto.response.OrderResponse;
import com.logistics.hub.feature.order.dto.response.OrderStatisticsResponse;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.mapper.OrderMapper;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.order.repository.OrderSpecification;
import com.logistics.hub.feature.order.service.OrderService;
import com.logistics.hub.feature.routing.service.impl.HaversineDistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final LocationService locationService;
    private final DepotRepository depotRepository;
    private final HaversineDistanceService haversineDistanceService;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;
    private final AuditActorService auditActorService;

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Pageable pageable, OrderStatus status, String search, Long depotId) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ORDER_READ);
        if (authorizationService.hasGlobalScope()) {
            return orderRepository.findAll(OrderSpecification.withFilters(status, search, depotId), pageable)
                    .map(this::toResponse);
        }

        if (depotId != null) {
            authorizationService.requireDepotAccess(depotId);
            return orderRepository.findAll(OrderSpecification.withFilters(status, search, depotId), pageable)
                    .map(this::toResponse);
        }

        if (authorizationService.getAccessibleDepotIds().isEmpty()) {
            return Page.empty(pageable);
        }

        return orderRepository.findAll(
                        OrderSpecification.withFilters(status, search, authorizationService.getAccessibleDepotIds()),
                        pageable)
                .map(this::toResponse);
    }

    @Override
    public OrderStatisticsResponse getStatistics() {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ORDER_READ);
        List<OrderEntity> allOrders = authorizationService.hasGlobalScope()
                ? orderRepository.findAll()
                : orderRepository.findAll(OrderSpecification.withFilters(null, null, authorizationService.getAccessibleDepotIds()));

        long activeCount = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.CANCELLED)
                .count();

        long pending = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CREATED)
                .count();

        long inTransit = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.IN_TRANSIT)
                .count();

        return OrderStatisticsResponse.builder()
                .total(activeCount)
                .pending(pending)
                .inTransit(inTransit)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id));
        authorizationService.requireOrderAccess(order);

        return orderRepository.findByIdWithLocation(id)
                .map(OrderResponse::fromProjection)
                .orElseThrow(() -> new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id));
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        try {
            authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ORDER_MANAGE);
            if (request.getDepotId() != null) {
                authorizationService.requireDepotAccess(request.getDepotId());
            } else if (!authorizationService.hasGlobalScope()) {
                throw new ValidationException("Scoped staff must choose a depot when creating an order.");
            }

            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                request.setCode(generateOrderCode());
            } else if (orderRepository.existsByCode(request.getCode())) {
                throw new ValidationException(OrderConstant.ORDER_CODE_EXISTS + request.getCode());
            }

            OrderEntity entity = orderMapper.toEntity(request);
            entity.setStatus(OrderStatus.CREATED);

            LocationEntity location = locationService.getOrCreateLocation(request.getDeliveryLocation());
            entity.setDeliveryLocation(location);

            if (request.getDepotId() == null) {
                assignNearestDepot(entity);
            } else {
                DepotEntity depot = depotRepository.findById(request.getDepotId())
                        .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + request.getDepotId()));
                authorizationService.requireDepotAccess(depot.getId());
                entity.setDepot(depot);
            }

            OrderEntity saved = orderRepository.save(entity);
            auditLogService.log(
                    auditActorService.getCurrentActor(),
                    AuditAction.CREATE,
                    AuditResourceType.ORDER,
                    saved.getId().toString(),
                    saved.getCode(),
                    saved.getDepot() != null ? saved.getDepot().getId() : null,
                    AuditStatus.SUCCESS,
                    "Created order",
                    null,
                    orderAuditSnapshot(saved),
                    Map.of("autoAssignedDepot", request.getDepotId() == null));
            return findById(saved.getId());
        } catch (RuntimeException ex) {
            logFailure(AuditAction.CREATE, null, request.getCode(), null, orderRequestSnapshot(request), ex);
            throw ex;
        }
    }

    private OrderResponse toResponse(OrderEntity entity) {
        OrderResponse response = orderMapper.toResponse(entity);

        if (entity.getDeliveryLocation() != null) {
            response.setDeliveryStreet(entity.getDeliveryLocation().getStreet());
            response.setDeliveryCity(entity.getDeliveryLocation().getCity());
            response.setDeliveryCountry(entity.getDeliveryLocation().getCountry());
            response.setDeliveryLocationName(String.format("%s, %s, %s",
                    entity.getDeliveryLocation().getStreet(),
                    entity.getDeliveryLocation().getCity(),
                    entity.getDeliveryLocation().getCountry()));
        }

        if (entity.getDriver() != null) {
            response.setDriverId(entity.getDriver().getId());
            response.setDriverName(entity.getDriver().getName());
        }

        return response;
    }

    @Override
    public OrderResponse update(Long id, OrderRequest request) {
        OrderEntity entity = null;
        Map<String, Object> beforeData = null;
        try {
            authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ORDER_MANAGE);
            entity = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id));
            authorizationService.requireOrderAccess(entity);
            beforeData = orderAuditSnapshot(entity);

            if (!entity.getCode().equals(request.getCode()) && orderRepository.existsByCode(request.getCode())) {
                throw new ValidationException(OrderConstant.ORDER_CODE_EXISTS + request.getCode());
            }

            orderMapper.updateEntityFromRequest(request, entity);

            if (request.getStatus() != null) {
                if (!authorizationService.hasPermission(AuthorizationPolicy.PERMISSION_ORDER_CANCEL_CONFIRMED)
                        && request.getStatus() == OrderStatus.CANCELLED
                        && entity.getStatus() != OrderStatus.CREATED) {
                    throw new ForbiddenException("Dispatcher cannot cancel a confirmed or active order.");
                }
                entity.setStatus(request.getStatus());
            }

            if (request.getDeliveryLocation() != null) {
                LocationEntity location = locationService.getOrCreateLocation(request.getDeliveryLocation());
                entity.setDeliveryLocation(location);

                if (request.getDepotId() == null) {
                    assignNearestDepot(entity);
                }
            }

            if (request.getDepotId() != null) {
                if (!authorizationService.hasPermission(AuthorizationPolicy.PERMISSION_VEHICLE_REASSIGN)
                        && entity.getDepot() != null
                        && !entity.getDepot().getId().equals(request.getDepotId())) {
                    throw new ForbiddenException("Moving an order to another depot requires admin approval.");
                }
                DepotEntity depot = depotRepository.findById(request.getDepotId())
                        .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + request.getDepotId()));
                authorizationService.requireDepotAccess(depot.getId());
                entity.setDepot(depot);
            }

            OrderEntity saved = orderRepository.save(entity);
            auditLogService.log(
                    auditActorService.getCurrentActor(),
                    AuditAction.UPDATE,
                    AuditResourceType.ORDER,
                    saved.getId().toString(),
                    saved.getCode(),
                    saved.getDepot() != null ? saved.getDepot().getId() : null,
                    AuditStatus.SUCCESS,
                    "Updated order",
                    beforeData,
                    orderAuditSnapshot(saved),
                    null);
            return findById(saved.getId());
        } catch (RuntimeException ex) {
            logFailure(AuditAction.UPDATE, id.toString(), entity != null ? entity.getCode() : request.getCode(), beforeData, orderRequestSnapshot(request), ex);
            throw ex;
        }
    }

    @Override
    public void updateStatusBulk(List<Long> orderIds, OrderStatus status) {
        try {
            List<OrderEntity> orders = orderRepository.findAllById(orderIds);

            authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ORDER_MANAGE);
            if (!authorizationService.hasPermission(AuthorizationPolicy.PERMISSION_ORDER_CANCEL_CONFIRMED) && status == OrderStatus.CANCELLED) {
                throw new ForbiddenException("Dispatcher cannot bulk cancel orders.");
            }

            if (orders.size() != orderIds.size()) {
                Set<Long> foundIds = orders.stream()
                        .map(OrderEntity::getId)
                        .collect(Collectors.toSet());

                String missingIds = orderIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));

                throw new ValidationException(OrderConstant.ORDER_IDS_NOT_FOUND + missingIds);
            }

            orders.forEach(authorizationService::requireOrderAccess);
            List<Map<String, Object>> beforeData = orders.stream()
                    .map(this::orderAuditSnapshot)
                    .toList();
            orders.forEach(order -> order.setStatus(status));
            orderRepository.saveAll(orders);
            auditLogService.log(
                    auditActorService.getCurrentActor(),
                    AuditAction.BULK_UPDATE,
                    AuditResourceType.ORDER,
                    String.valueOf(orderIds.size()),
                    "Bulk order status update",
                    resolveBulkScopeDepotId(orders),
                    AuditStatus.SUCCESS,
                    "Bulk updated order status",
                    beforeData,
                    orders.stream().map(this::orderAuditSnapshot).toList(),
                    Map.of("status", status.name(), "orderIds", orderIds));
        } catch (RuntimeException ex) {
            logFailure(AuditAction.BULK_UPDATE, String.valueOf(orderIds.size()), "Bulk order status update", null, Map.of("status", status.name(), "orderIds", orderIds), ex);
            throw ex;
        }
    }

    private void assignNearestDepot(OrderEntity order) {
        if (order.getDeliveryLocation() == null) {
            return;
        }

        List<DepotEntity> activeDepots = depotRepository.findAll().stream()
                .filter(DepotEntity::getIsActive)
                .filter(depot -> authorizationService.hasGlobalScope()
                        || authorizationService.getAccessibleDepotIds().contains(depot.getId()))
                .toList();

        if (activeDepots.isEmpty()) {
            log.warn("No active depots found to assign to order {}", order.getCode());
            return;
        }

        DepotEntity nearestDepot = null;
        double minDistance = Double.MAX_VALUE;

        for (DepotEntity depot : activeDepots) {
            try {
                double distance = haversineDistanceService.calculateDistance(
                        order.getDeliveryLocation(),
                        depot.getLocation()).getDistanceKm().doubleValue();

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestDepot = depot;
                }
            } catch (Exception e) {
                log.error("Error calculating distance between order {} and depot {}: {}",
                        order.getCode(), depot.getName(), e.getMessage());
            }
        }

        if (nearestDepot != null) {
            order.setDepot(nearestDepot);
            log.info("Automatically assigned depot {} to order {} (Distance: {} km)",
                    nearestDepot.getName(), order.getCode(), minDistance);
        }
    }

    @Override
    public void delete(Long id) {
        OrderEntity order = null;
        try {
            authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ORDER_MANAGE);
            order = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(OrderConstant.ORDER_NOT_FOUND + id));
            authorizationService.requireOrderAccess(order);
            order.markDeleted();
            orderRepository.save(order);
            auditLogService.log(
                    auditActorService.getCurrentActor(),
                    AuditAction.DELETE,
                    AuditResourceType.ORDER,
                    order.getId().toString(),
                    order.getCode(),
                    order.getDepot() != null ? order.getDepot().getId() : null,
                    AuditStatus.SUCCESS,
                    "Deleted order",
                    orderAuditSnapshot(order),
                    null,
                    null);
        } catch (RuntimeException ex) {
            logFailure(AuditAction.DELETE, id.toString(), order != null ? order.getCode() : null, order != null ? orderAuditSnapshot(order) : null, null, ex);
            throw ex;
        }
    }

    private String generateOrderCode() {
        return orderRepository.findTopByOrderByIdDesc()
                .map(lastOrder -> {
                    String lastCode = lastOrder.getCode();
                    if (lastCode.startsWith("ORD-")) {
                        try {
                            int sequence = Integer.parseInt(lastCode.substring(4));
                            return String.format("ORD-%03d", sequence + 1);
                        } catch (NumberFormatException e) {
                            return "ORD-001";
                        }
                    }
                    return "ORD-001";
                })
                .orElse("ORD-001");
    }

    private Long resolveBulkScopeDepotId(List<OrderEntity> orders) {
        return orders.stream()
                .map(order -> order.getDepot() != null ? order.getDepot().getId() : null)
                .distinct()
                .count() == 1
                ? orders.stream().map(order -> order.getDepot() != null ? order.getDepot().getId() : null).findFirst().orElse(null)
                : null;
    }

    private Map<String, Object> orderAuditSnapshot(OrderEntity entity) {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", entity.getId());
        snapshot.put("code", entity.getCode());
        snapshot.put("status", entity.getStatus() != null ? entity.getStatus().name() : null);
        snapshot.put("weightKg", entity.getWeightKg());
        snapshot.put("volumeM3", entity.getVolumeM3());
        snapshot.put("depotId", entity.getDepot() != null ? entity.getDepot().getId() : null);
        snapshot.put("depotName", entity.getDepot() != null ? entity.getDepot().getName() : null);
        snapshot.put("driverId", entity.getDriver() != null ? entity.getDriver().getId() : null);
        snapshot.put("driverName", entity.getDriver() != null ? entity.getDriver().getName() : null);
        snapshot.put("deliveryLocationId", entity.getDeliveryLocation() != null ? entity.getDeliveryLocation().getId() : null);
        snapshot.put("deliveryLocation", entity.getDeliveryLocation() == null ? null : Map.of(
                "street", entity.getDeliveryLocation().getStreet(),
                "city", entity.getDeliveryLocation().getCity(),
                "country", entity.getDeliveryLocation().getCountry()));
        return snapshot;
    }

    private Map<String, Object> orderRequestSnapshot(OrderRequest request) {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("code", request.getCode());
        snapshot.put("weightKg", request.getWeightKg());
        snapshot.put("volumeM3", request.getVolumeM3());
        snapshot.put("status", request.getStatus() != null ? request.getStatus().name() : null);
        snapshot.put("depotId", request.getDepotId());
        snapshot.put("deliveryLocation", request.getDeliveryLocation() == null ? null : Map.of(
                "street", request.getDeliveryLocation().getStreet(),
                "city", request.getDeliveryLocation().getCity(),
                "country", request.getDeliveryLocation().getCountry()));
        return snapshot;
    }

    private void logFailure(String action, String resourceId, String resourceName, Object beforeData, Object afterData, RuntimeException ex) {
        if (!(ex instanceof ValidationException || ex instanceof ForbiddenException || ex instanceof ResourceNotFoundException)) {
            return;
        }

        auditLogService.log(
                auditActorService.getCurrentActor(),
                action,
                AuditResourceType.ORDER,
                resourceId,
                resourceName,
                null,
                AuditStatus.FAILED,
                ex.getMessage(),
                beforeData,
                afterData,
                Map.of("exceptionType", ex.getClass().getSimpleName()));
    }
}
