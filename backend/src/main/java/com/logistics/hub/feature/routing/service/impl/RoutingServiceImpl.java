package com.logistics.hub.feature.routing.service.impl;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.google.protobuf.Duration;
import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.location.repository.LocationRepository;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.routing.constant.RoutingConstant;
import com.logistics.hub.feature.routing.config.RoutingConfig;
import com.logistics.hub.feature.routing.entity.RouteEntity;
import com.logistics.hub.feature.routing.entity.RouteStopEntity;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.enums.RouteStatus;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import com.logistics.hub.feature.routing.repository.RoutingRunRepository;
import com.logistics.hub.feature.routing.service.RoutingService;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingServiceImpl implements RoutingService {

    private final RoutingConfig routingConfig;
    private final OsrmDistanceService osrmDistanceService;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final LocationRepository locationRepository;
    private final DepotRepository depotRepository;
    private final RoutingRunRepository routingRunRepository;

    static {
        try {
            Loader.loadNativeLibraries();
        } catch (Exception e) {
            log.error("Failed to load OR-Tools native libraries", e);
            throw new RuntimeException("OR-Tools native libraries required", e);
        }
    }

    @Override
    public RoutingRunEntity optimizeRoutes(List<OrderEntity> orders, List<VehicleEntity> vehicles,
            List<LocationEntity> locations) {
        Long depotId = vehicles.get(0).getDepot() != null ? vehicles.get(0).getDepot().getId() : null;

        DepotEntity depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new ResourceNotFoundException(RoutingConstant.DEPOT_NOT_ASSIGNED + depotId));
        Long locationId = depot.getLocation() != null ? depot.getLocation().getId() : null;

        Map<Long, LocationEntity> locationMap = locations.stream()
                .collect(Collectors.toMap(LocationEntity::getId, l -> l));

        LocationEntity depotLocation = locationMap.get(locationId);
        if (depotLocation == null) {
            throw new ResourceNotFoundException(
                    RoutingConstant.DEPOT_LOCATION_NOT_FOUND + depotId + " (Location ID: " + locationId + ")");
        }

        Map<Long, List<OrderEntity>> ordersByLocation = orders.stream()
                .collect(Collectors
                        .groupingBy(o -> o.getDeliveryLocation() != null ? o.getDeliveryLocation().getId() : null));

        List<LocationEntity> nodeLocations = new ArrayList<>();
        List<List<OrderEntity>> orderGroupsByNode = new ArrayList<>();

        nodeLocations.add(depotLocation);
        orderGroupsByNode.add(Collections.emptyList());

        for (Map.Entry<Long, List<OrderEntity>> entry : ordersByLocation.entrySet()) {
            LocationEntity loc = locationMap.get(entry.getKey());
            if (loc == null) {
                throw new ResourceNotFoundException(
                        RoutingConstant.DELIVERY_LOCATION_NOT_FOUND + entry.getKey());
            }
            nodeLocations.add(loc);
            orderGroupsByNode.add(entry.getValue());
        }

        int nodeCount = nodeLocations.size();

        log.info("Building distance matrix for {} nodes (1 depot + {} delivery locations)",
                nodeCount, nodeCount - 1);

        com.logistics.hub.feature.routing.dto.response.MatrixResult matrixResult = osrmDistanceService
                .getMatrix(nodeLocations);
        long[][] distanceMatrix = matrixResult.getDistanceMatrix();
        int[][] durationMatrix = matrixResult.getDurationMatrix();

        long[] demandsWeight = new long[nodeCount];
        long[] demandsVolume = new long[nodeCount];

        demandsWeight[0] = 0;
        demandsVolume[0] = 0;

        for (int i = 1; i < nodeCount; i++) {
            List<OrderEntity> ordersAtLocation = orderGroupsByNode.get(i);

            long totalWeight = ordersAtLocation.stream()
                    .mapToLong(o -> o.getWeightKg() != null ? o.getWeightKg() : 0)
                    .sum();

            BigDecimal totalVolume = ordersAtLocation.stream()
                    .map(o -> o.getVolumeM3() != null ? o.getVolumeM3() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            demandsWeight[i] = totalWeight;
            int volumeScale = routingConfig.getSolver().getVolumeScalingFactor();
            demandsVolume[i] = totalVolume.multiply(BigDecimal.valueOf(volumeScale)).longValue();
        }

        int volumeScalingFactor = routingConfig.getSolver().getVolumeScalingFactor();

        int maxTripsPerVehicle = routingConfig.getSolver().getMaxTripsPerVehicle();
        int physicalVehicleCount = vehicles.size();
        int totalVirtualVehicles = physicalVehicleCount * maxTripsPerVehicle;

        long[] vehicleWeightCaps = new long[totalVirtualVehicles];
        long[] vehicleVolumeCaps = new long[totalVirtualVehicles];
        long[] vehicleFixedCosts = new long[totalVirtualVehicles];

        long baseFixedCost = routingConfig.getSolver().getVehicleFixedCost();

        for (int v = 0; v < physicalVehicleCount; v++) {
            VehicleEntity vehicle = vehicles.get(v);
            long weightCap = vehicle.getMaxWeightKg() != null ? vehicle.getMaxWeightKg() : Long.MAX_VALUE;
            long volumeCap = vehicle.getMaxVolumeM3() != null
                    ? vehicle.getMaxVolumeM3().multiply(BigDecimal.valueOf(volumeScalingFactor)).longValue()
                    : Long.MAX_VALUE;

            for (int trip = 0; trip < maxTripsPerVehicle; trip++) {
                int virtualIdx = (trip * physicalVehicleCount) + v;
                vehicleWeightCaps[virtualIdx] = weightCap;
                vehicleVolumeCaps[virtualIdx] = volumeCap;

                long tieredBase = baseFixedCost;
                if (trip == 1)
                    tieredBase = baseFixedCost * 10;
                else if (trip == 2)
                    tieredBase = baseFixedCost * 50;

                vehicleFixedCosts[virtualIdx] = tieredBase;
            }
        }

        RoutingIndexManager manager = new RoutingIndexManager(nodeCount, totalVirtualVehicles, 0);
        RoutingModel routing = new RoutingModel(manager);

        for (int i = 0; i < totalVirtualVehicles; i++) {
            routing.setFixedCostOfVehicle(vehicleFixedCosts[i], i);
        }

        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return distanceMatrix[fromNode][toNode];
        });
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        final int weightCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            return demandsWeight[fromNode];
        });
        routing.addDimensionWithVehicleCapacity(weightCallbackIndex,
                0,
                vehicleWeightCaps,
                true,
                "Weight");

        final int volumeCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            return demandsVolume[fromNode];
        });
        routing.addDimensionWithVehicleCapacity(volumeCallbackIndex,
                0,
                vehicleVolumeCaps,
                true,
                "Volume");

        log.info("Routing problem setup:");
        log.info("  - Nodes: {} (1 depot + {} deliveries)", nodeCount, nodeCount - 1);
        log.info("  - Physical Vehicles: {}", physicalVehicleCount);
        log.info("  - Total Virtual Vehicles (Trips): {}", totalVirtualVehicles);
        log.info("  - Node weight demands: {}", Arrays.toString(demandsWeight));
        log.info("  - Node volume demands: {}", Arrays.toString(demandsVolume));

        int timeLimitSeconds = routingConfig.getSolver().getTimeLimitSeconds();

        RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                .setTimeLimit(Duration.newBuilder().setSeconds(timeLimitSeconds).build())
                .build();

        log.info("Starting OR-Tools solver...");
        LocalDateTime solveStart = LocalDateTime.now();
        Assignment solution = routing.solveWithParameters(searchParameters);
        LocalDateTime solveEnd = LocalDateTime.now();
        long solveTimeMs = java.time.Duration.between(solveStart, solveEnd).toMillis();
        log.info("Solver completed in {} ms", solveTimeMs);

        RoutingRunEntity runEntity = new RoutingRunEntity();
        runEntity.setStartTime(LocalDateTime.now());

        if (solution != null) {
            runEntity.setStatus(RoutingRunStatus.COMPLETED);
            List<RouteEntity> routes = new ArrayList<>();
            long totalRunDistanceMeters = 0;
            BigDecimal totalRunCost = BigDecimal.ZERO;

            for (int i = 0; i < totalVirtualVehicles; i++) {
                long index = routing.start(i);

                if (routing.isEnd(solution.value(routing.nextVar(index)))) {
                    continue;
                }

                int physicalIdx = i % physicalVehicleCount;

                VehicleEntity vehicle = vehicles.get(physicalIdx);
                RouteEntity route = new RouteEntity();
                route.setRoutingRun(runEntity);
                route.setVehicle(vehicle);
                route.setStatus(RouteStatus.CREATED);

                List<RouteStopEntity> stops = new ArrayList<>();
                List<LocationEntity> routeWaypoints = new ArrayList<>();
                routeWaypoints.add(depotLocation);

                int sequence = 1;
                long routeDistanceMeters = 0;
                int routeDurationMinutes = 0;

                long prevIndex = index;
                index = solution.value(routing.nextVar(index));

                while (!routing.isEnd(index)) {
                    int nodeIndex = manager.indexToNode(index);
                    int prevNodeIndex = manager.indexToNode(prevIndex);

                    List<OrderEntity> ordersAtThisStop = orderGroupsByNode.get(nodeIndex);
                    LocationEntity stopLocation = nodeLocations.get(nodeIndex);
                    routeWaypoints.add(stopLocation);

                    long legDistanceMeters = distanceMatrix[prevNodeIndex][nodeIndex];
                    int legDurationMinutes = durationMatrix[prevNodeIndex][nodeIndex];

                    routeDistanceMeters += legDistanceMeters;
                    routeDurationMinutes += legDurationMinutes;

                    for (OrderEntity order : ordersAtThisStop) {
                        RouteStopEntity stop = new RouteStopEntity();
                        stop.setRoute(route);
                        stop.setStopSequence(sequence++);
                        stop.setLocation(stopLocation);
                        stop.setOrder(order);
                        stop.setDistanceFromPrevKm(BigDecimal.valueOf(legDistanceMeters / 1000.0));
                        stop.setDurationFromPrevMin(legDurationMinutes);

                        stops.add(stop);

                        if (vehicle.getDriver() != null) {
                            order.setDriver(vehicle.getDriver());
                        }
                    }

                    prevIndex = index;
                    index = solution.value(routing.nextVar(index));
                }

                int lastNodeIndex = manager.indexToNode(prevIndex);
                routeDistanceMeters += distanceMatrix[lastNodeIndex][0];
                routeDurationMinutes += durationMatrix[lastNodeIndex][0];
                routeWaypoints.add(depotLocation);

                String routePolyline = osrmDistanceService.getRoutePolyline(routeWaypoints);
                route.setPolyline(routePolyline);

                route.setTotalDistanceKm(BigDecimal.valueOf(routeDistanceMeters / 1000.0));
                route.setTotalDurationMin(routeDurationMinutes);

                if (vehicle.getCostPerKm() != null) {
                    BigDecimal routeCost = route.getTotalDistanceKm()
                            .multiply(vehicle.getCostPerKm())
                            .setScale(2, RoundingMode.HALF_UP);
                    route.setTotalCost(routeCost);
                    totalRunCost = totalRunCost.add(routeCost);
                }

                route.setStops(stops);
                routes.add(route);

                totalRunDistanceMeters += routeDistanceMeters;

                log.info("Route {}: Vehicle {}, {} stops, {} km, {} min, polyline: {}",
                        routes.size(), vehicle.getCode(), stops.size(),
                        route.getTotalDistanceKm(), route.getTotalDurationMin(),
                        routePolyline != null ? "present" : "null");
            }

            runEntity.setRoutes(routes);
            runEntity.setTotalDistanceKm(BigDecimal.valueOf(totalRunDistanceMeters / 1000.0));
            runEntity.setTotalCost(totalRunCost);

            String config = String.format(
                    "Solver: GUIDED_LOCAL_SEARCH | Strategy: PATH_CHEAPEST_ARC | TimeLimit: %ds | " +
                            "SolveTime: %dms | PhysicalVehicles: %d | VirtualVehicles: %d | RoutesUsed: %d | FixedCost: %d",
                    timeLimitSeconds, solveTimeMs, physicalVehicleCount, totalVirtualVehicles, routes.size(),
                    routingConfig.getSolver().getVehicleFixedCost());
            runEntity.setConfiguration(config);

            log.info("Optimization successful: {} routes created, Total: {} km, {} cost",
                    routes.size(), runEntity.getTotalDistanceKm(), runEntity.getTotalCost());
        } else {
            runEntity.setStatus(RoutingRunStatus.FAILED);
            log.warn("No solution found for routing optimization");
        }

        runEntity.setEndTime(LocalDateTime.now());

        return runEntity;
    }

    @Override
    @Transactional
    public RoutingRunEntity executeRouting(List<Long> orderIds, List<Long> vehicleIds) {
        log.info("Executing routing for {} orders and {} vehicles", count(orderIds), count(vehicleIds));

        List<OrderEntity> orders = orderRepository.findAllById(orderIds);
        List<VehicleEntity> vehicles = vehicleRepository.findAllById(vehicleIds);

        if (orders.isEmpty() || vehicles.isEmpty()) {
            throw new ValidationException(RoutingConstant.ORDER_IDS_EMPTY);
        }

        Long depotId = vehicles.get(0).getDepot() != null ? vehicles.get(0).getDepot().getId() : null;
        if (depotId == null) {
            throw new ValidationException(RoutingConstant.DEPOT_NOT_ASSIGNED);
        }

        boolean multipleDepots = vehicles.stream()
                .map(v -> v.getDepot() != null ? v.getDepot().getId() : null)
                .anyMatch(id -> !Objects.equals(id, depotId));

        if (multipleDepots) {
            throw new ValidationException(RoutingConstant.MULTIPLE_DEPOTS_ERROR);
        }

        // Verify all orders are assigned to this depot
        boolean wrongDepot = orders.stream()
                .map(o -> o.getDepot() != null ? o.getDepot().getId() : null)
                .anyMatch(id -> id != null && !Objects.equals(id, depotId));

        if (wrongDepot) {
            throw new ValidationException("Một số đơn hàng được gán cho kho khác với kho của đội xe.");
        }

        DepotEntity depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new ValidationException(RoutingConstant.DEPOT_NOT_ASSIGNED + depotId));
        Long locationId = depot.getLocation() != null ? depot.getLocation().getId() : null;

        Set<Long> locationIds = new HashSet<>();
        locationIds.add(locationId);
        orders.forEach(o -> locationIds.add(o.getDeliveryLocation() != null ? o.getDeliveryLocation().getId() : null));

        List<LocationEntity> locations = locationRepository.findAllById(locationIds);

        if (locations.size() != locationIds.size()) {
            throw new ValidationException(RoutingConstant.LOCATIONS_NOT_FOUND);
        }

        RoutingRunEntity result = optimizeRoutes(orders, vehicles, locations);

        if (result.getStatus() == RoutingRunStatus.COMPLETED) {
            orders.forEach(order -> order.setStatus(OrderStatus.IN_TRANSIT));
            orderRepository.saveAll(orders);
            log.info("Updated {} orders to IN_TRANSIT status", orders.size());
        }

        return routingRunRepository.save(result);
    }

    @Override
    @Transactional
    public RoutingRunEntity executeAutoRouting() {
        log.info("Executing auto-routing: fetching available orders and vehicles");

        // For auto-routing, we pick the first depot with active vehicles and created
        // orders
        List<VehicleEntity> activeVehicles = vehicleRepository.findByStatusAndDriverIdNotNull(VehicleStatus.ACTIVE);
        if (activeVehicles.isEmpty()) {
            throw new ValidationException(RoutingConstant.VEHICLES_NOT_FOUND);
        }

        Long depotId = activeVehicles.get(0).getDepot() != null ? activeVehicles.get(0).getDepot().getId() : null;
        if (depotId == null) {
            throw new ValidationException(RoutingConstant.DEPOT_NOT_ASSIGNED);
        }

        List<VehicleEntity> vehicles = activeVehicles.stream()
                .filter(v -> v.getDepot() != null && Objects.equals(v.getDepot().getId(), depotId))
                .collect(Collectors.toList());

        List<OrderEntity> orders = orderRepository.findByStatus(OrderStatus.CREATED).stream()
                .filter(o -> o.getDepot() != null && Objects.equals(o.getDepot().getId(), depotId))
                .collect(Collectors.toList());

        if (orders.isEmpty()) {
            throw new ValidationException("Không tìm thấy đơn hàng CREATED nào được gán cho kho ID: " + depotId);
        }

        log.info("Found {} orders and {} vehicles for depot ID: {}", orders.size(), vehicles.size(), depotId);

        List<Long> orderIds = orders.stream().map(OrderEntity::getId).collect(Collectors.toList());
        List<Long> vehicleIds = vehicles.stream().map(VehicleEntity::getId).collect(Collectors.toList());

        return executeRouting(orderIds, vehicleIds);
    }

    private int count(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
