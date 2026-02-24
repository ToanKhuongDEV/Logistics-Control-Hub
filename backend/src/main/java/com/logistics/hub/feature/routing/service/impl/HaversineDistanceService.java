package com.logistics.hub.feature.routing.service.impl;

import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.routing.config.RoutingConfig;
import com.logistics.hub.feature.routing.dto.response.DistanceResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class HaversineDistanceService {

    private final RoutingConfig routingConfig;

    public DistanceResult calculateDistance(LocationEntity origin, LocationEntity destination) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origin and Destination must not be null");
        }

        double earthRadiusKm = routingConfig.getDistance().getEarthRadiusKm();
        double averageSpeedKmh = routingConfig.getDistance().getAverageSpeedKmh();

        double lat1 = Math.toRadians(origin.getLatitude());
        double lat2 = Math.toRadians(destination.getLatitude());
        double lon1 = Math.toRadians(origin.getLongitude());
        double lon2 = Math.toRadians(destination.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distanceKm = earthRadiusKm * c;

        double durationHours = distanceKm / averageSpeedKmh;
        int durationMinutes = (int) Math.ceil(durationHours * 60);

        BigDecimal distance = BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP);

        log.debug("Haversine: {}, {} -> {}, {} = {} km, {} min",
                origin.getStreet(), origin.getCity(),
                destination.getStreet(), destination.getCity(),
                distance, durationMinutes);

        return new DistanceResult(distance, durationMinutes, null);
    }

    public com.logistics.hub.feature.routing.dto.response.MatrixResult calculateMatrix(
            java.util.List<LocationEntity> locations) {
        int size = locations.size();
        long[][] distances = new long[size][size];
        int[][] durations = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    distances[i][j] = 0;
                    durations[i][j] = 0;
                } else {
                    DistanceResult result = calculateDistance(locations.get(i), locations.get(j));
                    distances[i][j] = result.getDistanceKm().multiply(java.math.BigDecimal.valueOf(1000)).longValue();
                    durations[i][j] = result.getDurationMinutes();
                }
            }
        }
        return new com.logistics.hub.feature.routing.dto.response.MatrixResult(distances, durations);
    }
}
