package com.logistics.hub.feature.excel.service.impl;

import com.logistics.hub.common.constant.TemplatePath;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.common.util.ExcelUtils;
import com.logistics.hub.common.util.ExcelUtils.ColumnAlignment;
import com.logistics.hub.common.util.ExcelUtils.ExcelColumn;
import com.logistics.hub.feature.auth.policy.AuthorizationPolicy;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.depot.dto.response.DepotResponse;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.depot.mapper.DepotMapper;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.driver.dto.response.DriverResponse;
import com.logistics.hub.feature.driver.entity.DriverEntity;
import com.logistics.hub.feature.driver.mapper.DriverMapper;
import com.logistics.hub.feature.driver.repository.DriverRepository;
import com.logistics.hub.feature.excel.enums.ExcelFileEnum;
import com.logistics.hub.feature.excel.service.ExcelExportService;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.dto.response.OrderResponse;
import com.logistics.hub.feature.order.enums.OrderStatus;
import com.logistics.hub.feature.order.mapper.OrderMapper;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.routing.entity.RouteEntity;
import com.logistics.hub.feature.routing.entity.RouteStopEntity;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import com.logistics.hub.feature.routing.repository.RoutingRunRepository;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
import com.logistics.hub.feature.vehicle.dto.response.VehicleResponse;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import com.logistics.hub.feature.vehicle.mapper.VehicleMapper;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final int DEFAULT_MAX_ROWS = 100;
    private static final int MAX_EXPORT_ROWS = 50_000;
    private static final String REPORT_SHEET = "Report";

    private final DepotRepository depotRepository;
    private final DepotMapper depotMapper;
    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final RoutingRunRepository routingRunRepository;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ByteArrayResource> export(
            ExcelFileEnum type,
            String search,
            String status,
            Long depotId,
            LocalDate fromDate,
            LocalDate toDate,
            Integer maxRows
    ) throws IOException {
        if (type == null) {
            throw new ValidationException("Excel export type is required.");
        }
        validateDateRange(fromDate, toDate);

        return switch (type) {
            case DEPOT -> exportDepots(search, fromDate, toDate, maxRows);
            case DRIVER -> exportDrivers(search, depotId, fromDate, toDate, maxRows);
            case ORDER -> exportOrders(search, status, depotId, fromDate, toDate, maxRows);
            case ROUTING -> exportRoutingRuns(status, depotId, fromDate, toDate, maxRows);
            case VEHICLE -> exportVehicles(search, status, depotId, fromDate, toDate, maxRows);
        };
    }

    private ResponseEntity<ByteArrayResource> exportDepots(
            String search,
            LocalDate fromDate,
            LocalDate toDate,
            Integer maxRows
    ) throws IOException {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DEPOT_READ);
        List<DepotResponse> rows = depotRepository.findAll(depotSpec(search, fromDate, toDate), exportPageable(maxRows))
                .map(depotMapper::toResponse)
                .getContent();
        ByteArrayResource resource = exportFromTemplate(TemplatePath.DEPOT, rows, depotColumns());
        return ExcelUtils.download(filename("depots"), resource);
    }

    private ResponseEntity<ByteArrayResource> exportDrivers(
            String search,
            Long depotId,
            LocalDate fromDate,
            LocalDate toDate,
            Integer maxRows
    )
            throws IOException {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_READ);
        List<DriverResponse> rows = driverRepository
                .findAll(driverSpec(search, depotId, fromDate, toDate), exportPageable(maxRows))
                .map(driverMapper::toResponse)
                .getContent();
        ByteArrayResource resource = exportFromTemplate(TemplatePath.DRIVER, rows, driverColumns());
        return ExcelUtils.download(filename("drivers"), resource);
    }

    private ResponseEntity<ByteArrayResource> exportOrders(
            String search,
            String status,
            Long depotId,
            LocalDate fromDate,
            LocalDate toDate,
            Integer maxRows
    ) throws IOException {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ORDER_READ);
        OrderStatus orderStatus = parseStatus(status, OrderStatus.class, ExcelFileEnum.ORDER);
        List<OrderResponse> rows = orderRepository
                .findAll(orderSpec(search, orderStatus, depotId, fromDate, toDate), exportPageable(maxRows))
                .map(this::toOrderResponse)
                .getContent();
        ByteArrayResource resource = exportFromTemplate(TemplatePath.ORDER, rows, orderColumns());
        return ExcelUtils.download(filename("orders"), resource);
    }

    private ResponseEntity<ByteArrayResource> exportVehicles(
            String search,
            String status,
            Long depotId,
            LocalDate fromDate,
            LocalDate toDate,
            Integer maxRows
    ) throws IOException {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_VEHICLE_READ);
        VehicleStatus vehicleStatus = parseStatus(status, VehicleStatus.class, ExcelFileEnum.VEHICLE);
        List<VehicleResponse> rows = vehicleRepository
                .findAll(vehicleSpec(search, vehicleStatus, depotId, fromDate, toDate), exportPageable(maxRows))
                .map(vehicleMapper::toResponse)
                .getContent();
        ByteArrayResource resource = exportFromTemplate(TemplatePath.VEHICLE, rows, vehicleColumns());
        return ExcelUtils.download(filename("vehicles"), resource);
    }

    private ResponseEntity<ByteArrayResource> exportRoutingRuns(
            String status,
            Long depotId,
            LocalDate fromDate,
            LocalDate toDate,
            Integer maxRows
    ) throws IOException {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ROUTING_READ);
        RoutingRunStatus routingStatus = parseStatus(status, RoutingRunStatus.class, ExcelFileEnum.ROUTING);
        List<RoutingRunEntity> runs = routingRunRepository
                .findAll(routingSpec(routingStatus, depotId, fromDate, toDate), exportPageable(maxRows))
                .getContent();

        List<RoutingExportRow> rows = flattenRoutingRows(runs);
        ByteArrayResource resource = exportFromTemplate(TemplatePath.ROUTING, rows, routingColumns());
        return ExcelUtils.download(filename("routing-runs"), resource);
    }

    private <T> ByteArrayResource exportFromTemplate(
            TemplatePath templatePath,
            Collection<T> rows,
            List<ExcelColumn<T>> columns
    ) throws IOException {
        return ExcelUtils.exportReportFromTemplate(
                templatePath.getExportPath(),
                REPORT_SHEET,
                0,
                1,
                rows,
                columns);
    }

    private Pageable exportPageable(Integer maxRows) {
        int normalizedMaxRows = normalizeMaxRows(maxRows);
        return PageRequest.of(0, normalizedMaxRows, Sort.by(Sort.Order.desc("createdAt")));
    }

    private int normalizeMaxRows(Integer maxRows) {
        if (maxRows == null) {
            return DEFAULT_MAX_ROWS;
        }
        if (maxRows <= 0) {
            throw new ValidationException("maxRows must be greater than 0.");
        }
        return Math.min(maxRows, MAX_EXPORT_ROWS);
    }

    private <E extends Enum<E>> E parseStatus(String status, Class<E> enumClass, ExcelFileEnum type) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(enumClass, status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Invalid " + type.name().toLowerCase() + " status: " + status);
        }
    }

    private String filename(String prefix) {
        return prefix + "-" + LocalDate.now() + ".xlsx";
    }

    private List<RoutingExportRow> flattenRoutingRows(List<RoutingRunEntity> runs) {
        List<RoutingExportRow> rows = new ArrayList<>();
        for (RoutingRunEntity run : runs) {
            if (run.getRoutes() == null || run.getRoutes().isEmpty()) {
                rows.add(new RoutingExportRow(run, null, null));
                continue;
            }

            for (RouteEntity route : run.getRoutes()) {
                if (route.getStops() == null || route.getStops().isEmpty()) {
                    rows.add(new RoutingExportRow(run, route, null));
                    continue;
                }

                for (RouteStopEntity stop : route.getStops()) {
                    rows.add(new RoutingExportRow(run, route, stop));
                }
            }
        }
        return rows;
    }

    private Specification<DepotEntity> depotSpec(String search, LocalDate fromDate, LocalDate toDate) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String keyword = likeKeyword(search);
                var location = root.join("location", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), keyword),
                        cb.like(cb.lower(location.get("street")), keyword),
                        cb.like(cb.lower(location.get("city")), keyword),
                        cb.like(cb.lower(location.get("country")), keyword)));
            }

            if (!authorizationService.hasGlobalScope()) {
                Set<Long> depotIds = authorizationService.getAccessibleDepotIds();
                if (depotIds.isEmpty()) {
                    return cb.disjunction();
                }
                predicates.add(root.get("id").in(depotIds));
            }

            addLocalDateTimeRange(predicates, cb, root.get("createdAt"), fromDate, toDate);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<DriverEntity> driverSpec(
            String search,
            Long depotId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String keyword = likeKeyword(search);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), keyword),
                        cb.like(cb.lower(root.get("licenseNumber")), keyword),
                        cb.like(cb.lower(root.get("phoneNumber")), keyword)));
            }

            Collection<Long> depotIds = resolveDepotScope(depotId);
            if (depotIds != null) {
                if (depotIds.isEmpty()) {
                    return cb.disjunction();
                }
                var vehicleSubquery = query.subquery(Long.class);
                var vehicle = vehicleSubquery.from(VehicleEntity.class);
                vehicleSubquery.select(vehicle.get("id"))
                        .where(
                                cb.equal(vehicle.get("driver"), root),
                                vehicle.get("depot").get("id").in(depotIds));
                predicates.add(cb.exists(vehicleSubquery));
            }

            addInstantRange(predicates, cb, root.get("createdAt"), fromDate, toDate);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<OrderEntity> orderSpec(
            String search,
            OrderStatus status,
            Long depotId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("code")), likeKeyword(search)));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            Collection<Long> depotIds = resolveDepotScope(depotId);
            if (depotIds != null) {
                if (depotIds.isEmpty()) {
                    return cb.disjunction();
                }
                predicates.add(root.get("depot").get("id").in(depotIds));
            }

            addInstantRange(predicates, cb, root.get("createdAt"), fromDate, toDate);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<VehicleEntity> vehicleSpec(
            String search,
            VehicleStatus status,
            Long depotId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("code")), likeKeyword(search)));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            Collection<Long> depotIds = resolveDepotScope(depotId);
            if (depotIds != null) {
                if (depotIds.isEmpty()) {
                    return cb.disjunction();
                }
                predicates.add(root.get("depot").get("id").in(depotIds));
            }

            addInstantRange(predicates, cb, root.get("createdAt"), fromDate, toDate);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<RoutingRunEntity> routingSpec(
            RoutingRunStatus status,
            Long depotId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            Collection<Long> depotIds = resolveDepotScope(depotId);
            if (depotIds != null) {
                if (depotIds.isEmpty()) {
                    return cb.disjunction();
                }
                predicates.add(root.get("depot").get("id").in(depotIds));
            }

            addLocalDateTimeRange(predicates, cb, root.get("createdAt"), fromDate, toDate);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Collection<Long> resolveDepotScope(Long depotId) {
        if (depotId != null) {
            authorizationService.requireDepotAccess(depotId);
            return List.of(depotId);
        }
        if (authorizationService.hasGlobalScope()) {
            return null;
        }
        return authorizationService.getAccessibleDepotIds();
    }

    private OrderResponse toOrderResponse(OrderEntity entity) {
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

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException("fromDate must be before or equal to toDate.");
        }
    }

    private void addInstantRange(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Path<Instant> path,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, startInstant(fromDate)));
        }
        if (toDate != null) {
            predicates.add(cb.lessThan(path, endExclusiveInstant(toDate)));
        }
    }

    private void addLocalDateTimeRange(
            List<Predicate> predicates,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Path<LocalDateTime> path,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(path, fromDate.atStartOfDay()));
        }
        if (toDate != null) {
            predicates.add(cb.lessThan(path, toDate.plusDays(1).atStartOfDay()));
        }
    }

    private Instant startInstant(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant endExclusiveInstant(LocalDate date) {
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private String likeKeyword(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }

    private List<ExcelColumn<DepotResponse>> depotColumns() {
        return List.of(
                ExcelUtils.column("ID", DepotResponse::getId, 10, ColumnAlignment.CENTER),
                ExcelUtils.column("Depot Name", DepotResponse::getName, 24, ColumnAlignment.LEFT),
                ExcelUtils.column("Location ID", DepotResponse::getLocationId, 12, ColumnAlignment.CENTER),
                ExcelUtils.column("Street", DepotResponse::getStreet, 28, ColumnAlignment.LEFT),
                ExcelUtils.column("City", DepotResponse::getCity, 18, ColumnAlignment.LEFT),
                ExcelUtils.column("Country", DepotResponse::getCountry, 18, ColumnAlignment.LEFT),
                ExcelUtils.column("Description", DepotResponse::getDescription, 32, ColumnAlignment.LEFT),
                ExcelUtils.column("Active", DepotResponse::getIsActive, 10, ColumnAlignment.CENTER),
                ExcelUtils.column("Created At", DepotResponse::getCreatedAt, 20, ColumnAlignment.CENTER));
    }

    private List<ExcelColumn<DriverResponse>> driverColumns() {
        return List.of(
                ExcelUtils.column("ID", DriverResponse::getId, 10, ColumnAlignment.CENTER),
                ExcelUtils.column("Driver Name", DriverResponse::getName, 24, ColumnAlignment.LEFT),
                ExcelUtils.column("License Number", DriverResponse::getLicenseNumber, 18, ColumnAlignment.LEFT),
                ExcelUtils.column("Phone Number", DriverResponse::getPhoneNumber, 18, ColumnAlignment.LEFT),
                ExcelUtils.column("Email", DriverResponse::getEmail, 28, ColumnAlignment.LEFT),
                ExcelUtils.column("Created At", DriverResponse::getCreatedAt, 20, ColumnAlignment.CENTER),
                ExcelUtils.column("Updated At", DriverResponse::getUpdatedAt, 20, ColumnAlignment.CENTER));
    }

    private List<ExcelColumn<OrderResponse>> orderColumns() {
        return List.of(
                ExcelUtils.column("ID", OrderResponse::getId, 10, ColumnAlignment.CENTER),
                ExcelUtils.column("Order Code", OrderResponse::getCode, 18, ColumnAlignment.LEFT),
                ExcelUtils.column("Delivery Street", OrderResponse::getDeliveryStreet, 28, ColumnAlignment.LEFT),
                ExcelUtils.column("Delivery City", OrderResponse::getDeliveryCity, 18, ColumnAlignment.LEFT),
                ExcelUtils.column("Delivery Country", OrderResponse::getDeliveryCountry, 18, ColumnAlignment.LEFT),
                ExcelUtils.column("Weight (kg)", OrderResponse::getWeightKg, 14, ColumnAlignment.RIGHT),
                ExcelUtils.column("Volume (m3)", OrderResponse::getVolumeM3, 14, ColumnAlignment.RIGHT),
                ExcelUtils.column("Driver ID", OrderResponse::getDriverId, 12, ColumnAlignment.CENTER),
                ExcelUtils.column("Driver Name", OrderResponse::getDriverName, 24, ColumnAlignment.LEFT),
                ExcelUtils.column("Depot ID", OrderResponse::getDepotId, 12, ColumnAlignment.CENTER),
                ExcelUtils.column("Depot Name", OrderResponse::getDepotName, 24, ColumnAlignment.LEFT),
                ExcelUtils.column("Latitude", OrderResponse::getLatitude, 14, ColumnAlignment.RIGHT),
                ExcelUtils.column("Longitude", OrderResponse::getLongitude, 14, ColumnAlignment.RIGHT),
                ExcelUtils.column("Status", OrderResponse::getStatus, 18, ColumnAlignment.CENTER),
                ExcelUtils.column("Created At", OrderResponse::getCreatedAt, 20, ColumnAlignment.CENTER));
    }

    private List<ExcelColumn<VehicleResponse>> vehicleColumns() {
        return List.of(
                ExcelUtils.column("ID", VehicleResponse::getId, 10, ColumnAlignment.CENTER),
                ExcelUtils.column("Vehicle Code", VehicleResponse::getCode, 18, ColumnAlignment.LEFT),
                ExcelUtils.column("Type", VehicleResponse::getType, 16, ColumnAlignment.LEFT),
                ExcelUtils.column("Max Weight (kg)", VehicleResponse::getMaxWeightKg, 16, ColumnAlignment.RIGHT),
                ExcelUtils.column("Max Volume (m3)", VehicleResponse::getMaxVolumeM3, 16, ColumnAlignment.RIGHT),
                ExcelUtils.column("Cost/km", VehicleResponse::getCostPerKm, 14, ColumnAlignment.RIGHT),
                ExcelUtils.column("Status", VehicleResponse::getStatus, 16, ColumnAlignment.CENTER),
                ExcelUtils.column("Driver ID", VehicleResponse::getDriverId, 12, ColumnAlignment.CENTER),
                ExcelUtils.column("Driver Name", VehicleResponse::getDriverName, 24, ColumnAlignment.LEFT),
                ExcelUtils.column("Depot ID", VehicleResponse::getDepotId, 12, ColumnAlignment.CENTER),
                ExcelUtils.column("Depot Name", VehicleResponse::getDepotName, 24, ColumnAlignment.LEFT),
                ExcelUtils.column("Depot Street", VehicleResponse::getStreet, 28, ColumnAlignment.LEFT),
                ExcelUtils.column("Depot City", VehicleResponse::getCity, 18, ColumnAlignment.LEFT),
                ExcelUtils.column("Depot Country", VehicleResponse::getCountry, 18, ColumnAlignment.LEFT),
                ExcelUtils.column("Created At", VehicleResponse::getCreatedAt, 20, ColumnAlignment.CENTER));
    }

    private List<ExcelColumn<RoutingExportRow>> routingColumns() {
        return List.of(
                ExcelUtils.column("Run ID", row -> row.run().getId(), 10, ColumnAlignment.CENTER),
                ExcelUtils.column("Run Status", row -> row.run().getStatus(), 16, ColumnAlignment.CENTER),
                ExcelUtils.column("Depot ID", row -> row.run().getDepot() != null ? row.run().getDepot().getId() : null,
                        12, ColumnAlignment.CENTER),
                ExcelUtils.column("Depot Name",
                        row -> row.run().getDepot() != null ? row.run().getDepot().getName() : null,
                        24, ColumnAlignment.LEFT),
                ExcelUtils.column("Start Time", row -> row.run().getStartTime(), 20, ColumnAlignment.CENTER),
                ExcelUtils.column("End Time", row -> row.run().getEndTime(), 20, ColumnAlignment.CENTER),
                ExcelUtils.column("Total Distance (km)", row -> row.run().getTotalDistanceKm(), 20,
                        ColumnAlignment.RIGHT),
                ExcelUtils.column("Total Cost", row -> row.run().getTotalCost(), 16, ColumnAlignment.RIGHT),
                ExcelUtils.column("Route ID", row -> row.route() != null ? row.route().getId() : null, 10,
                        ColumnAlignment.CENTER),
                ExcelUtils.column("Vehicle ID",
                        row -> row.route() != null && row.route().getVehicle() != null
                                ? row.route().getVehicle().getId()
                                : null,
                        12, ColumnAlignment.CENTER),
                ExcelUtils.column("Route Status", row -> row.route() != null ? row.route().getStatus() : null, 16,
                        ColumnAlignment.CENTER),
                ExcelUtils.column("Route Distance (km)",
                        row -> row.route() != null ? row.route().getTotalDistanceKm() : null,
                        20, ColumnAlignment.RIGHT),
                ExcelUtils.column("Route Duration (min)",
                        row -> row.route() != null ? row.route().getTotalDurationMin() : null,
                        20, ColumnAlignment.RIGHT),
                ExcelUtils.column("Route Cost", row -> row.route() != null ? row.route().getTotalCost() : null, 16,
                        ColumnAlignment.RIGHT),
                ExcelUtils.column("Stop Sequence", row -> row.stop() != null ? row.stop().getStopSequence() : null,
                        16, ColumnAlignment.CENTER),
                ExcelUtils.column("Order ID",
                        row -> row.stop() != null && row.stop().getOrder() != null ? row.stop().getOrder().getId()
                                : null,
                        12, ColumnAlignment.CENTER),
                ExcelUtils.column("Location ID",
                        row -> row.stop() != null && row.stop().getLocation() != null
                                ? row.stop().getLocation().getId()
                                : null,
                        12, ColumnAlignment.CENTER),
                ExcelUtils.column("Latitude",
                        row -> row.stop() != null && row.stop().getLocation() != null
                                ? row.stop().getLocation().getLatitude()
                                : null,
                        14, ColumnAlignment.RIGHT),
                ExcelUtils.column("Longitude",
                        row -> row.stop() != null && row.stop().getLocation() != null
                                ? row.stop().getLocation().getLongitude()
                                : null,
                        14, ColumnAlignment.RIGHT),
                ExcelUtils.column("Distance From Previous (km)",
                        row -> row.stop() != null ? row.stop().getDistanceFromPrevKm() : null,
                        26, ColumnAlignment.RIGHT),
                ExcelUtils.column("Duration From Previous (min)",
                        row -> row.stop() != null ? row.stop().getDurationFromPrevMin() : null,
                        28, ColumnAlignment.RIGHT));
    }

    private record RoutingExportRow(RoutingRunEntity run, RouteEntity route, RouteStopEntity stop) {
    }
}
