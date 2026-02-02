package com.logistics.hub.feature.routing.service.impl;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.location.repository.LocationRepository;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.routing.entity.RouteEntity;
import com.logistics.hub.feature.routing.entity.RouteStopEntity;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.enums.RouteStatus;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import com.logistics.hub.feature.routing.repository.RoutingRunRepository;
import com.logistics.hub.feature.routing.service.DistanceService;
import com.logistics.hub.feature.routing.service.RoutingService;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingServiceImpl implements RoutingService {

    private final DistanceService distanceService;
    private final LocationRepository locationRepository;
    private final RoutingRunRepository routingRunRepository;

    // Static block to load native libraries once
    static {
        try {
            Loader.loadNativeLibraries();
        } catch (Exception e) {
            log.error("Failed to load OR-Tools native libraries", e);
            throw new RuntimeException("OR-Tools native libraries required", e);
        }
    }

    @Override
    @Transactional
    public RoutingRunEntity optimizeRoutes(List<OrderEntity> orders, List<VehicleEntity> vehicles) {
        if (orders.isEmpty() || vehicles.isEmpty()) {
            throw new IllegalArgumentException("Orders and Vehicles must not be empty");
        }

        // 1. Data Preparation
        Long depotId = vehicles.get(0).getDepotId();
        if (depotId == null) {
            throw new IllegalArgumentException("Vehicle must have a depot (current implementation assumes single depot)");
        }

        // Fetch all necessary locations
        Set<Long> locationIds = new HashSet<>();
        locationIds.add(depotId);
        orders.forEach(o -> locationIds.add(o.getDeliveryLocationId()));
        
        Map<Long, LocationEntity> locationMap = locationRepository.findAllById(locationIds)
                .stream().collect(Collectors.toMap(LocationEntity::getId, l -> l));

        LocationEntity depotLocation = locationMap.get(depotId);
        if (depotLocation == null) throw new IllegalStateException("Depot location not found: " + depotId);

        // List of Locations in Node Index Order
        List<LocationEntity> nodeLocations = new ArrayList<>();
        nodeLocations.add(depotLocation); // Node 0
        
        // Map Orders to Nodes
        for (OrderEntity order : orders) {
            LocationEntity loc = locationMap.get(order.getDeliveryLocationId());
            if (loc == null) throw new IllegalStateException("Location not found for order " + order.getCode());
            nodeLocations.add(loc); // Node i
        }

        int nodeCount = nodeLocations.size();
        int vehicleCount = vehicles.size();

        // 2. Build Distance Matrix (Meters)
        long[][] distanceMatrix = new long[nodeCount][nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < nodeCount; j++) {
                if (i == j) {
                    distanceMatrix[i][j] = 0;
                } else {
                    BigDecimal distKm = distanceService.getDistanceKm(nodeLocations.get(i), nodeLocations.get(j));
                     // Convert to meters (long)
                    distanceMatrix[i][j] = distKm.multiply(BigDecimal.valueOf(1000)).longValue();
                }
            }
        }

        // 3. Prepare Capacities (Weight & Volume)
        long[] demandsWeight = new long[nodeCount];
        long[] demandsVolume = new long[nodeCount];
        
        // Depot has 0 demand
        demandsWeight[0] = 0;
        demandsVolume[0] = 0;

        for (int i = 0; i < orders.size(); i++) {
            OrderEntity order = orders.get(i);
            int nodeIndex = i + 1;
            demandsWeight[nodeIndex] = order.getWeightKg() != null ? order.getWeightKg() : 0;
            // Scale volume by 1000 to convert m3 to dm3 (avoid precision loss for values < 1)
            demandsVolume[nodeIndex] = order.getVolumeM3() != null ? order.getVolumeM3().multiply(BigDecimal.valueOf(1000)).longValue() : 0;
        }

        long[] vehicleWeightCaps = vehicles.stream().mapToLong(v -> v.getMaxWeightKg() != null ? v.getMaxWeightKg() : Long.MAX_VALUE).toArray();
        long[] vehicleVolumeCaps = vehicles.stream().mapToLong(v -> v.getMaxVolumeM3() != null ? v.getMaxVolumeM3().multiply(BigDecimal.valueOf(1000)).longValue() : Long.MAX_VALUE).toArray();

        // 4. Initialize OR-Tools
        RoutingIndexManager manager = new RoutingIndexManager(nodeCount, vehicleCount, 0); // 0 is depot
        RoutingModel routing = new RoutingModel(manager);

        // Set Fixed Cost for all vehicles to encourage using fewer vehicles
        // Cost should be significantly higher than average route distance cost
        for (int i = 0; i < vehicleCount; i++) {
            routing.setFixedCostOfVehicle(1_000_000, i);
        }

        // 5. Cost Function (Distance)
        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return distanceMatrix[fromNode][toNode];
        });
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // 6. Dimensions
        // Weight
        final int weightCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            return demandsWeight[fromNode];
        });
        routing.addDimensionWithVehicleCapacity(
            weightCallbackIndex,
            0, 
            vehicleWeightCaps, 
            true, 
            "Weight"
        );

        // Volume
        final int volumeCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            return demandsVolume[fromNode];
        });
        routing.addDimensionWithVehicleCapacity(
            volumeCallbackIndex,
            0,
            vehicleVolumeCaps,
            true,
            "Volume"
        );

        // 7. Solver Parameters
        RoutingSearchParameters searchParameters =
            main.defaultRoutingSearchParameters()
                .toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                .setTimeLimit(com.google.protobuf.Duration.newBuilder().setSeconds(5).build()) 
                .build();

        // 8. Solve
        Assignment solution = routing.solveWithParameters(searchParameters);

        // 9. Process Result
        RoutingRunEntity runEntity = new RoutingRunEntity();
        runEntity.setStartTime(Instant.now());
        
        if (solution != null) {
            runEntity.setStatus(RoutingRunStatus.COMPLETED);
            List<RouteEntity> routes = new ArrayList<>();
            long totalRunDistanceMeters = 0;

            for (int i = 0; i < vehicleCount; i++) {
                long index = routing.start(i);
                if (routing.isEnd(solution.value(routing.nextVar(index)))) {
                    // Vehicle not used
                    continue;
                }

                RouteEntity route = new RouteEntity();
                route.setRoutingRun(runEntity);
                route.setVehicleId(vehicles.get(i).getId());
                route.setStatus(RouteStatus.CREATED);
                
                List<RouteStopEntity> stops = new ArrayList<>();
                int sequence = 1;
                long routeDistanceMeters = 0;

                // Move from start
                long prevIndex = index;
                index = solution.value(routing.nextVar(index)); // First actual stop (or end)

                while (!routing.isEnd(index)) {
                    int nodeIndex = manager.indexToNode(index);
                    
                    OrderEntity matchedOrder = orders.get(nodeIndex - 1); // Map back to order
                    
                    long legDistance = routing.getArcCostForVehicle(prevIndex, index, i);
                    routeDistanceMeters += legDistance;

                    RouteStopEntity stop = new RouteStopEntity();
                    stop.setRoute(route);
                    stop.setStopSequence(sequence++);
                    stop.setLocationId(matchedOrder.getDeliveryLocationId());
                    stop.setOrderId(matchedOrder.getId());
                    stop.setDistanceFromPrevKm(BigDecimal.valueOf(legDistance / 1000.0));
                    
                    stops.add(stop);

                    prevIndex = index;
                    index = solution.value(routing.nextVar(index));
                }
                
                // Add return to depot leg distance
                long returnDistance = routing.getArcCostForVehicle(prevIndex, index, i);
                routeDistanceMeters += returnDistance;

                route.setTotalDistanceKm(BigDecimal.valueOf(routeDistanceMeters / 1000.0));
                route.setStops(stops);
                routes.add(route);
                
                totalRunDistanceMeters += routeDistanceMeters;
            }
            runEntity.setRoutes(routes);
            runEntity.setTotalDistanceKm(BigDecimal.valueOf(totalRunDistanceMeters / 1000.0));
        } else {
            runEntity.setStatus(RoutingRunStatus.FAILED);
            log.warn("No solution found for routing optimization");
        }
        
        runEntity.setEndTime(Instant.now());
        
        // 10. Persist Result
        return routingRunRepository.save(runEntity);
    }
}
