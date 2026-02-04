package com.logistics.hub.feature.routing.mapper;

import com.logistics.hub.feature.routing.dto.response.RouteResponse;
import com.logistics.hub.feature.routing.dto.response.RouteStopResponse;
import com.logistics.hub.feature.routing.dto.response.RoutingRunResponse;
import com.logistics.hub.feature.routing.entity.RouteEntity;
import com.logistics.hub.feature.routing.entity.RouteStopEntity;
import com.logistics.hub.feature.routing.entity.RoutingRunEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class RoutingMapper {

    private RoutingMapper() {
    }

    public static RoutingRunResponse toRoutingRunResponse(RoutingRunEntity entity) {
        if (entity == null) {
            return null;
        }

        RoutingRunResponse response = new RoutingRunResponse();
        response.setId(entity.getId());
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        response.setTotalDistanceKm(entity.getTotalDistanceKm());
        response.setTotalCost(entity.getTotalCost());
        response.setConfiguration(entity.getConfiguration());
        response.setCreatedAt(entity.getCreatedAt());

        if (entity.getRoutes() != null) {
            List<RouteResponse> routes = entity.getRoutes().stream()
                    .map(RoutingMapper::toRouteResponse)
                    .collect(Collectors.toList());
            response.setRoutes(routes);
        } else {
            response.setRoutes(Collections.emptyList());
        }

        return response;
    }

    public static RouteResponse toRouteResponse(RouteEntity entity) {
        if (entity == null) {
            return null;
        }

        RouteResponse response = new RouteResponse();
        response.setId(entity.getId());
        response.setVehicleId(entity.getVehicleId());
        response.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        // address field will be handled by higher level or mapper if needed,
        // using entity.getVehicle().getDepot().getAddress() if available
        response.setTotalDistanceKm(entity.getTotalDistanceKm());
        response.setTotalDurationMin(entity.getTotalDurationMin());
        response.setTotalCost(entity.getTotalCost());
        response.setPolyline(entity.getPolyline());

        if (entity.getStops() != null) {
            List<RouteStopResponse> stops = entity.getStops().stream()
                    .map(RoutingMapper::toRouteStopResponse)
                    .collect(Collectors.toList());
            response.setStops(stops);
        } else {
            response.setStops(Collections.emptyList());
        }

        return response;
    }

    public static RouteStopResponse toRouteStopResponse(RouteStopEntity entity) {
        if (entity == null) {
            return null;
        }

        RouteStopResponse response = new RouteStopResponse();
        response.setId(entity.getId());
        response.setLocationId(entity.getLocationId());
        response.setOrderId(entity.getOrderId());
        response.setStopSequence(entity.getStopSequence());
        response.setDistanceFromPrevKm(entity.getDistanceFromPrevKm());
        response.setDurationFromPrevMin(entity.getDurationFromPrevMin());

        return response;
    }
}
