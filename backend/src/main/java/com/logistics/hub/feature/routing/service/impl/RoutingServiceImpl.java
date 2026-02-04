package com.logistics.hub.feature.routing.service.impl;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.routing.config.RoutingConfig;
import com.logistics.hub.feature.routing.dto.response.DistanceResult;
import com.logistics.hub.feature.routing.entity.RouteEntity;
import com.logistics.hub.feature.routing.entity.RouteStopEntity;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.enums.RouteStatus;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import com.logistics.hub.feature.location.repository.LocationRepository;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation cho routing optimization
 * Sử dụng Google OR-Tools để giải bài toán VRP (Vehicle Routing Problem)
 * 
 * Quy trình:
 * 1. Nhận input: orders, vehicles, locations
 * 2. Xây dựng distance matrix từ OSRM
 * 3. Thiết lập constraints (capacity, volume)
 * 4. Chạy solver để tìm routes tối ưu
 * 5. Parse kết quả và lưu vào database
 */
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
    private final RoutingRunRepository routingRunRepository;

    /**
     * Static block để load OR-Tools native libraries
     * Phải load trước khi sử dụng bất kỳ class nào của OR-Tools
     */
    static {
        try {
            Loader.loadNativeLibraries();
        } catch (Exception e) {
            log.error("Failed to load OR-Tools native libraries", e);
            throw new RuntimeException("OR-Tools native libraries required", e);
        }
    }

    /**
     * Tối ưu hóa routes cho danh sách orders và vehicles
     * 
     * @param orders    Danh sách orders cần giao
     * @param vehicles  Danh sách vehicles có sẵn
     * @param locations Danh sách tất cả locations (bao gồm depot và delivery
     *                  points)
     * @return RoutingRunEntity chứa các routes đã tối ưu
     * @throws IllegalStateException nếu không tìm thấy depot hoặc delivery location
     */
    @Override
    public RoutingRunEntity optimizeRoutes(List<OrderEntity> orders, List<VehicleEntity> vehicles,
            List<LocationEntity> locations) {
        // Lấy depot ID từ vehicle đầu tiên (tất cả vehicles phải cùng depot)
        Long depotId = vehicles.get(0).getDepotId();

        // Tạo map để tra cứu location nhanh
        Map<Long, LocationEntity> locationMap = locations.stream()
                .collect(Collectors.toMap(LocationEntity::getId, l -> l));

        // Lấy depot location
        LocationEntity depotLocation = locationMap.get(depotId);
        if (depotLocation == null) {
            throw new IllegalStateException("Depot location not found: " + depotId);
        }

        // Group orders theo delivery location để giảm số nodes
        // Nhiều orders cùng địa điểm sẽ được giao cùng lúc tại 1 stop
        Map<Long, List<OrderEntity>> ordersByLocation = orders.stream()
                .collect(Collectors.groupingBy(OrderEntity::getDeliveryLocationId));

        // Tạo danh sách nodes cho routing
        // Node 0: Depot
        // Node 1..N: Delivery locations
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

        // ========== Xây dựng Distance và Duration Matrix ==========
        // Matrix[i][j] = khoảng cách/thời gian từ node i đến node j
        long[][] distanceMatrix = new long[nodeCount][nodeCount];
        int[][] durationMatrix = new int[nodeCount][nodeCount];

        for (int i = 0; i < nodeCount; i++) {
            for (int j = 0; j < nodeCount; j++) {
                if (i == j) {
                    // Khoảng cách từ điểm tới chính nó = 0
                    distanceMatrix[i][j] = 0;
                    durationMatrix[i][j] = 0;
                } else {
                    // Gọi OSRM để lấy khoảng cách và thời gian thực tế
                    DistanceResult result = osrmDistanceService.getDistanceWithDuration(
                            nodeLocations.get(i), nodeLocations.get(j));
                    // OR-Tools yêu cầu distance bằng meters (integer)
                    distanceMatrix[i][j] = result.getDistanceKm().multiply(BigDecimal.valueOf(1000)).longValue();
                    durationMatrix[i][j] = result.getDurationMinutes();
                }
            }
        }

        // ========== Tính toán Demands cho mỗi node ==========
        // Demands = tải trọng cần giao tại mỗi điểm
        long[] demandsWeight = new long[nodeCount];
        long[] demandsVolume = new long[nodeCount];

        // Node 0 (depot) không có demand
        demandsWeight[0] = 0;
        demandsVolume[0] = 0;

        // Tính tổng demand cho mỗi delivery location
        for (int i = 1; i < nodeCount; i++) {
            List<OrderEntity> ordersAtLocation = orderGroupsByNode.get(i);

            // Tổng khối lượng (kg)
            long totalWeight = ordersAtLocation.stream()
                    .mapToLong(o -> o.getWeightKg() != null ? o.getWeightKg() : 0)
                    .sum();

            // Tổng thể tích (m³)
            BigDecimal totalVolume = ordersAtLocation.stream()
                    .map(o -> o.getVolumeM3() != null ? o.getVolumeM3() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            demandsWeight[i] = totalWeight;
            // Scale volume lên để tránh mất dữ liệu khi cast sang long
            // Ví dụ: 1.5 m³ = 1500 units (với scale factor 1000)
            int volumeScale = routingConfig.getSolver().getVolumeScalingFactor();
            demandsVolume[i] = totalVolume.multiply(BigDecimal.valueOf(volumeScale)).longValue();
        }

        // ========== Tính toán Vehicle Capacities ==========
        // Lấy capacity của từng xe (weight và volume)
        long[] vehicleWeightCaps = vehicles.stream()
                .mapToLong(v -> v.getMaxWeightKg() != null ? v.getMaxWeightKg() : Long.MAX_VALUE)
                .toArray();

        // Scale volume capacity giống như demands
        int volumeScale = routingConfig.getSolver().getVolumeScalingFactor();
        long[] vehicleVolumeCaps = vehicles.stream()
                .mapToLong(v -> v.getMaxVolumeM3() != null
                        ? v.getMaxVolumeM3().multiply(BigDecimal.valueOf(volumeScale)).longValue()
                        : Long.MAX_VALUE)
                .toArray();

        // ========== Khởi tạo OR-Tools Routing Model ==========
        // Create routing index manager
        // Parameters: nodeCount, vehicleCount, depotIndex (node 0)
        RoutingIndexManager manager = new RoutingIndexManager(nodeCount, vehicleCount, 0);
        RoutingModel routing = new RoutingModel(manager);

        // Đặt fixed cost cho mỗi vehicle để khuyến khích sử dụng ít xe hơn
        // Nếu không có fixed cost, solver có thể dùng nhiều xe mặc dù không cần thiết
        long vehicleFixedCost = routingConfig.getSolver().getVehicleFixedCost();
        for (int i = 0; i < vehicleCount; i++) {
            routing.setFixedCostOfVehicle(vehicleFixedCost, i);
        }

        // ========== Thiết lập Transit Callback (Cost Function) ==========
        // Callback này trả về chi phí (khoảng cách) để di chuyển từ fromIndex sang
        // toIndex
        // OR-Tools sử dụng để tính tổng chi phí của route
        final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            int toNode = manager.indexToNode(toIndex);
            return distanceMatrix[fromNode][toNode];
        });
        // Set arc cost evaluator = distance cho tất cả vehicles
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // ========== Thêm Dimension: Weight Capacity ==========
        // Dimension cho capacity constraint (khối lượng)
        final int weightCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            return demandsWeight[fromNode];
        });
        routing.addDimensionWithVehicleCapacity(
                weightCallbackIndex, // Callback trả về demand tại mỗi node
                0, // Slack max (không cho phép slack)
                vehicleWeightCaps, // Capacity của mỗi vehicle
                true, // Start cumul to zero (bắt đầu từ 0)
                "Weight" // Tên dimension
        );

        // ========== Thêm Dimension: Volume Capacity ==========
        // Dimension cho capacity constraint (thể tích)
        final int volumeCallbackIndex = routing.registerUnaryTransitCallback((long fromIndex) -> {
            int fromNode = manager.indexToNode(fromIndex);
            return demandsVolume[fromNode];
        });
        routing.addDimensionWithVehicleCapacity(
                volumeCallbackIndex, // Callback trả về demand tại mỗi node
                0, // Slack max
                vehicleVolumeCaps, // Capacity của mỗi vehicle
                true, // Start cumul to zero
                "Volume" // Tên dimension
        );

        // ========== Cấu hình Solver Parameters ==========
        // Đọc cấu hình từ application.yml
        int timeLimitSeconds = routingConfig.getSolver().getTimeLimitSeconds();

        RoutingSearchParameters searchParameters = main.defaultRoutingSearchParameters()
                .toBuilder()
                // Chiến lược tạo solution ban đầu: chọn arc rẻ nhất
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
                // Metaheuristic để cải thiện solution: Guided Local Search
                .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                // Thời gian giới hạn cho solver (có thể config trong application.yml)
                .setTimeLimit(com.google.protobuf.Duration.newBuilder().setSeconds(timeLimitSeconds).build())
                .build();

        // ========== Chạy Solver ==========
        log.info("Starting OR-Tools solver...");
        LocalDateTime solveStart = LocalDateTime.now();
        Assignment solution = routing.solveWithParameters(searchParameters);
        LocalDateTime solveEnd = LocalDateTime.now();
        long solveTimeMs = Duration.between(solveStart, solveEnd).toMillis();
        log.info("Solver completed in {} ms", solveTimeMs);

        // ========== Chuẩn bị kết quả Routing Run ==========
        RoutingRunEntity runEntity = new RoutingRunEntity();
        runEntity.setStartTime(LocalDateTime.now());

        if (solution != null) {
            // Nếu tìm được solution, parse kết quả
            runEntity.setStatus(RoutingRunStatus.COMPLETED);
            List<RouteEntity> routes = new ArrayList<>();
            long totalRunDistanceMeters = 0;
            int totalRunDurationMinutes = 0;
            BigDecimal totalRunCost = BigDecimal.ZERO;

            // ========== Duyệt qua từng vehicle để tạo routes ==========
            for (int i = 0; i < vehicleCount; i++) {
                long index = routing.start(i);

                // Kiểm tra xem vehicle này có được sử dụng không
                // Nếu next của start là end, vehicle không được sử dụng
                if (routing.isEnd(solution.value(routing.nextVar(index)))) {
                    continue;
                }

                VehicleEntity vehicle = vehicles.get(i);
                RouteEntity route = new RouteEntity();
                route.setRoutingRun(runEntity);
                route.setVehicleId(vehicle.getId());
                route.setStatus(RouteStatus.CREATED);

                List<RouteStopEntity> stops = new ArrayList<>();
                List<LocationEntity> routeSequence = new ArrayList<>(); // Để lấy polyline

                int sequence = 1; // Thứ tự stop bắt đầu từ 1
                long routeDistanceMeters = 0;
                int routeDurationMinutes = 0;

                // Thêm depot vào đầu sequence
                routeSequence.add(depotLocation);

                // Bắt đầu từ depot, di chuyển tới node tiếp theo
                long prevIndex = index;
                index = solution.value(routing.nextVar(index));

                // ========== Duyệt qua từng stop trong route ==========
                while (!routing.isEnd(index)) {
                    int nodeIndex = manager.indexToNode(index);
                    int prevNodeIndex = manager.indexToNode(prevIndex);

                    // Lấy thông tin về stop này
                    List<OrderEntity> ordersAtThisStop = orderGroupsByNode.get(nodeIndex);
                    LocationEntity stopLocation = nodeLocations.get(nodeIndex);

                    // Thêm location vào sequence để lấy polyline sau
                    routeSequence.add(stopLocation);

                    // Lấy khoảng cách và thời gian từ stop trước
                    long legDistanceMeters = distanceMatrix[prevNodeIndex][nodeIndex];
                    int legDurationMinutes = durationMatrix[prevNodeIndex][nodeIndex];

                    // Cộng dồn vào tổng route
                    routeDistanceMeters += legDistanceMeters;
                    routeDurationMinutes += legDurationMinutes;

                    // Tạo RouteStop cho mỗi order tại location này
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

                    // Di chuyển tới stop tiếp theo
                    prevIndex = index;
                    index = solution.value(routing.nextVar(index));
                }

                // ========== Xử lý chặng trở về depot ==========
                // Sau khi giao xong tất cả orders, xe phải quay về depot
                int prevNodeIndex = manager.indexToNode(prevIndex);
                long returnDistance = distanceMatrix[prevNodeIndex][0]; // Về node 0 (depot)
                int returnDuration = durationMatrix[prevNodeIndex][0];
                routeDistanceMeters += returnDistance;
                routeDurationMinutes += returnDuration;

                // Thêm depot vào cuối sequence (trở về)
                routeSequence.add(depotLocation);

                // ========== Lấy polyline cho toàn bộ route ==========
                String routePolyline = routePolylineService.getRoutePolyline(routeSequence);
                route.setPolyline(routePolyline);

                // ========== Lưu thông tin tổng hợp của route ==========
                route.setTotalDistanceKm(BigDecimal.valueOf(routeDistanceMeters / 1000.0));
                route.setTotalDurationMin(routeDurationMinutes);

                // Tính chi phí route (nếu vehicle có costPerKm)
                if (vehicle.getCostPerKm() != null) {
                    BigDecimal routeCost = route.getTotalDistanceKm()
                            .multiply(vehicle.getCostPerKm())
                            .setScale(2, RoundingMode.HALF_UP);
                    route.setTotalCost(routeCost);
                    totalRunCost = totalRunCost.add(routeCost);
                }

                route.setStops(stops);
                routes.add(route);

                // Cộng dồn vào tổng run
                totalRunDistanceMeters += routeDistanceMeters;
                totalRunDurationMinutes += routeDurationMinutes;

                log.info("Route {}: Vehicle {}, {} stops, {} km, {} min, polyline: {}",
                        routes.size(), vehicle.getCode(), stops.size(),
                        route.getTotalDistanceKm(), route.getTotalDurationMin(),
                        routePolyline != null ? "present" : "null");
            }

            // ========== Lưu kết quả tổng hợp ==========
            runEntity.setRoutes(routes);
            runEntity.setTotalDistanceKm(BigDecimal.valueOf(totalRunDistanceMeters / 1000.0));
            runEntity.setTotalCost(totalRunCost);

            // Tạo configuration string để ghi lại tham số đã sử dụng
            String config = String.format(
                    "Solver: GUIDED_LOCAL_SEARCH | Strategy: PATH_CHEAPEST_ARC | TimeLimit: %ds | " +
                            "SolveTime: %dms | VehiclesProvided: %d | VehiclesUsed: %d | FixedCost: %d",
                    timeLimitSeconds, solveTimeMs, vehicleCount, routes.size(),
                    routingConfig.getSolver().getVehicleFixedCost());
            runEntity.setConfiguration(config);

            log.info("Optimization successful: {} routes created, Total: {} km, {} cost",
                    routes.size(), runEntity.getTotalDistanceKm(), runEntity.getTotalCost());
        } else {
            // Nếu không tìm được solution
            runEntity.setStatus(RoutingRunStatus.FAILED);
            log.warn("No solution found for routing optimization");
        }

        runEntity.setEndTime(LocalDateTime.now());

        return runEntity;
    }

    /**
     * Execute routing optimization với validation và database interaction
     * Đây là method public được gọi từ controller/service khác
     * 
     * Flow:
     * 1. Fetch entities từ database theo IDs
     * 2. Validate input (tồn tại, same depot,...)
     * 3. Gọi optimizeRoutes() để chạy optimization
     * 4. Lưu kết quả vào database
     * 
     * @param orderIds   Danh sách IDs của orders cần optimize
     * @param vehicleIds Danh sách IDs của vehicles có thể dùng
     * @return RoutingRunEntity đã save vào database
     * @throws IllegalArgumentException nếu input không hợp lệ
     * @throws IllegalStateException    nếu có vấn đề về data consistency
     */
    @Override
    @Transactional
    public RoutingRunEntity executeRouting(List<Long> orderIds, List<Long> vehicleIds) {
        log.info("Executing routing for {} orders and {} vehicles", count(orderIds), count(vehicleIds));

        // ========== Fetch entities từ database ==========
        List<OrderEntity> orders = orderRepository.findAllById(orderIds);
        List<VehicleEntity> vehicles = vehicleRepository.findAllById(vehicleIds);

        // ========== Validation: Empty check ==========
        if (orders.isEmpty() || vehicles.isEmpty()) {
            throw new IllegalArgumentException("Orders and Vehicles must not be empty");
        }

        // Warning nếu một số orders không tìm thấy
        if (orders.size() != orderIds.size()) {
            log.warn("Some orders were not found. Requested: {}, Found: {}", orderIds.size(), orders.size());
        }

        // ========== Validation: Depot check ==========
        // Tất cả vehicles phải thuộc cùng một depot
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

        // ========== Collect tất cả location IDs cần thiết ==========
        // Bao gồm: depot + tất cả delivery locations
        Set<Long> locationIds = new HashSet<>();
        locationIds.add(depotId);
        orders.forEach(o -> locationIds.add(o.getDeliveryLocationId()));

        List<LocationEntity> locations = locationRepository.findAllById(locationIds);

        // Validation: Tất cả locations phải tồn tại
        if (locations.size() != locationIds.size()) {
            throw new IllegalStateException("Not all locations (Depot + Delivery Points) could be found. Expected: "
                    + locationIds.size() + ", Found: " + locations.size());
        }

        // ========== Gọi optimization engine ==========
        RoutingRunEntity result = optimizeRoutes(orders, vehicles, locations);

        // ========== Lưu kết quả vào database ==========
        return routingRunRepository.save(result);
    }

    /**
     * Helper method để đếm size của list (null-safe)
     */
    private int count(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
