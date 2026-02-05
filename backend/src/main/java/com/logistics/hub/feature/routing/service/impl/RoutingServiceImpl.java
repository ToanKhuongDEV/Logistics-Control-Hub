package com.logistics.hub.feature.routing.service.impl;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.google.protobuf.Duration;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.location.repository.LocationRepository;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.routing.config.RoutingConfig;
import com.logistics.hub.feature.routing.dto.response.DistanceResult;
import com.logistics.hub.feature.routing.entity.RouteEntity;
import com.logistics.hub.feature.routing.entity.RouteStopEntity;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.enums.RouteStatus;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import com.logistics.hub.feature.routing.repository.RoutingRunRepository;
import com.logistics.hub.feature.routing.service.RoutingService;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
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
    private final RoutePolylineService routePolylineService;
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
        Long depotId = vehicles.get(0).getDepotId();

        DepotEntity depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new IllegalStateException("Depot not found: " + depotId));
        Long locationId = depot.getLocationId();

        Map<Long, LocationEntity> locationMap = locations.stream()
                .collect(Collectors.toMap(LocationEntity::getId, l -> l));

        LocationEntity depotLocation = locationMap.get(locationId);
        if (depotLocation == null) {
            throw new IllegalStateException(
                    "Depot location not found for depot " + depotId + " (Location ID: " + locationId + ")");
        }

        Map<Long, List<OrderEntity>> ordersByLocation = orders.stream()
                .collect(Collectors.groupingBy(OrderEntity::getDeliveryLocationId));

        List<LocationEntity> nodeLocations = new ArrayList<>();
        List<List<OrderEntity>> orderGroupsByNode = new ArrayList<>();

        nodeLocations.add(depotLocation);
        orderGroupsByNode.add(Collections.emptyList());

        for (Map.Entry<Long, List<OrderEntity>> entry : ordersByLocation.entrySet()) {
            LocationEntity loc = locationMap.get(entry.getKey());
            if (loc == null) {
                throw new IllegalStateException("Location not found for delivery location ID: " + entry.getKey());
            }
            nodeLocations.add(loc);
            orderGroupsByNode.add(entry.getValue());
        }

        int nodeCount = nodeLocations.size();
        int vehicleCount = vehicles.size();

        log.info("Building distance matrix for {} nodes (1 depot + {} delivery locations)",
                nodeCount, nodeCount - 1);

        long[][] distanceMatrix = new long[nodeCount][nodeCount];
        int[][] durationMatrix = new int[nodeCount][nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < nodeCount; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                    durationMatrix[i][j] = 0;
                } else {
                    DistanceResult result = osrmDistanceService.getDistanceWithDuration(
                            nodeLocations.get(i), nodeLocations.get(j));
                    distanceMatrix[i][j] = result.getDistanceKm().multiply(BigDecimal.valueOf(1000)).longValue();
                    durationMatrix[i][j] = result.getDurationMinutes();
                }
            }
        }

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

        long[] vehicleWeightCaps = vehicles.stream()
                .mapToLong(v -> v.getMaxWeightKg() != null ? v.getMaxWeightKg() : Long.MAX_VALUE)
                .toArray();

        int volumeScalingFactor = routingConfig.getSolver().getVolumeScalingFactor();
        long[] vehicleVolumeCaps = vehicles.stream()
                .mapToLong(v -> v.getMaxVolumeM3() != null
                        ? v.getMaxVolumeM3().multiply(BigDecimal.valueOf(volumeScalingFactor)).longValue()
                        : Long.MAX_VALUE)
                .toArray();

        RoutingIndexManager manager = new RoutingIndexManager(nodeCount, vehicleCount, 0);
        RoutingModel routing = new RoutingModel(manager);

        long vehicleFixedCost = routingConfig.getSolver().getVehicleFixedCost();
        for (int i = 0; i < vehicleCount; i++) {
            routing.setFixedCostOfVehicle(vehicleFixedCost, i);
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

            for (int i = 0; i < vehicleCount; i++) {
                long index = routing.start(i);

                if (routing.isEnd(solution.value(routing.nextVar(index)))) {
                    continue;
                }

                VehicleEntity vehicle = vehicles.get(i);
                RouteEntity route = new RouteEntity();
                route.setRoutingRun(runEntity);
                route.setVehicleId(vehicle.getId());
                route.setStatus(RouteStatus.CREATED);

                List<RouteStopEntity> stops = new ArrayList<>();
                List<LocationEntity> routeSequence = new ArrayList<>();

                int sequence = 1;
                long routeDistanceMeters = 0;
                int routeDurationMinutes = 0;

                routeSequence.add(depotLocation);

                long prevIndex = index;
                index = solution.value(routing.nextVar(index));

                while (!routing.isEnd(index)) {
                    int nodeIndex = manager.indexToNode(index);
                    int prevNodeIndex = manager.indexToNode(prevIndex);

                    List<OrderEntity> ordersAtThisStop = orderGroupsByNode.get(nodeIndex);
                    LocationEntity stopLocation = nodeLocations.get(nodeIndex);

                    routeSequence.add(stopLocation);

                    long legDistanceMeters = distanceMatrix[prevNodeIndex][nodeIndex];
                    int legDurationMinutes = durationMatrix[prevNodeIndex][nodeIndex];

                    routeDistanceMeters += legDistanceMeters;
                    routeDurationMinutes += legDurationMinutes;

                    for (OrderEntity order : ordersAtThisStop) {
                        RouteStopEntity stop = new RouteStopEntity();
                        stop.setRoute(route);
                        stop.setStopSequence(sequence++);
                        stop.setLocationId(stopLocation.getId());
                        stop.setOrderId(order.getId());
                        stop.setDistanceFromPrevKm(BigDecimal.valueOf(legDistanceMeters / 1000.0));
                        stop.setDurationFromPrevMin(legDurationMinutes);

                        stops.add(stop);
                    }

                    prevIndex = index;
                    index = solution.value(routing.nextVar(index));
                }

                int prevNodeIndex = manager.indexToNode(prevIndex);
                long returnDistance = distanceMatrix[prevNodeIndex][0];
                int returnDuration = durationMatrix[prevNodeIndex][0];
                routeDistanceMeters += returnDistance;
                routeDurationMinutes += returnDuration;

                routeSequence.add(depotLocation);

                String routePolyline = routePolylineService.getRoutePolyline(routeSequence);
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
                            "SolveTime: %dms | VehiclesProvided: %d | VehiclesUsed: %d | FixedCost: %d",
                    timeLimitSeconds, solveTimeMs, vehicleCount, routes.size(),
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
            throw new IllegalArgumentException("Orders and Vehicles must not be empty");
        }

        if (orders.size() != orderIds.size()) {
            log.warn("Some orders were not found. Requested: {}, Found: {}", orderIds.size(), orders.size());
        }

        Long depotId = vehicles.get(0).getDepotId();
        if (depotId == null) {
            throw new IllegalArgumentException("Vehicle must have a depot assigned");
        }

        boolean multipleDepots = vehicles.stream()
                .map(VehicleEntity::getDepotId)
                .anyMatch(id -> !Objects.equals(id, depotId));

        if (multipleDepots) {
            throw new IllegalArgumentException(
                    "All vehicles in a single optimization run must belong to the same depot");
        }

        DepotEntity depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new IllegalArgumentException("Depot not found: " + depotId));
        Long locationId = depot.getLocationId();

        Set<Long> locationIds = new HashSet<>();
        locationIds.add(locationId);
        orders.forEach(o -> locationIds.add(o.getDeliveryLocationId()));

        List<LocationEntity> locations = locationRepository.findAllById(locationIds);

        if (locations.size() != locationIds.size()) {
            throw new IllegalStateException("Not all locations (Depot + Delivery Points) could be found. Expected: "
                    + locationIds.size() + ", Found: " + locations.size());
        }

        RoutingRunEntity result = optimizeRoutes(orders, vehicles, locations);

        return routingRunRepository.save(result);
    }

    private int count(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
