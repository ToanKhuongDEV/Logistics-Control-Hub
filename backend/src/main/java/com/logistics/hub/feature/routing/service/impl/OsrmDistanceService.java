package com.logistics.hub.feature.routing.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.routing.dto.response.DistanceResult;
import com.logistics.hub.feature.routing.dto.response.MatrixResult;
import com.logistics.hub.feature.routing.service.DistanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class OsrmDistanceService implements DistanceService {

    @Value("${osrm.url}")
    private String osrmUrl;

    private final HaversineDistanceService haversineDistanceService;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BigDecimal getDistanceKm(LocationEntity origin, LocationEntity destination) {
        DistanceResult result = getDistanceWithDuration(origin, destination);
        return result.getDistanceKm();
    }

    public DistanceResult getDistanceWithDuration(LocationEntity origin, LocationEntity destination) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origin and Destination must not be null");
        }

        try {
            String coordinates = String.format(Locale.US, "%f,%f;%f,%f",
                    origin.getLongitude(), origin.getLatitude(),
                    destination.getLongitude(), destination.getLatitude());

            String url = String.format("%s/route/v1/driving/%s?overview=full&geometries=polyline", osrmUrl,
                    coordinates);

            log.debug("Calling OSRM: {}", url);
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode routes = root.path("routes");

                if (routes.isArray() && routes.size() > 0) {
                    JsonNode route = routes.get(0);

                    double distanceMeters = route.path("distance").asDouble();
                    double durationSeconds = route.path("duration").asDouble();

                    String polyline = route.path("geometry").asText(null);

                    BigDecimal distanceKm = BigDecimal.valueOf(distanceMeters / 1000.0)
                            .setScale(2, RoundingMode.HALF_UP);
                    int durationMinutes = (int) Math.ceil(durationSeconds / 60.0);

                    log.debug("OSRM: {}, {} -> {}, {} = {} km, {} min, polyline: {}",
                            origin.getStreet(), origin.getCity(),
                            destination.getStreet(), destination.getCity(),
                            distanceKm, durationMinutes,
                            polyline != null ? "present" : "null");

                    return new DistanceResult(distanceKm, durationMinutes, polyline);
                }
            }
        } catch (Exception e) {
            log.warn("OSRM API failed for {}, {} -> {}, {}, falling back to Haversine: {}",
                    origin.getStreet(), origin.getCity(),
                    destination.getStreet(), destination.getCity(),
                    e.getMessage());
            return haversineDistanceService.calculateDistance(origin, destination);
        }

        log.warn("OSRM returned no routes for {}, {} -> {}, {}, using Haversine fallback",
                origin.getStreet(), origin.getCity(),
                destination.getStreet(), destination.getCity());
        return haversineDistanceService.calculateDistance(origin, destination);
    }

    public MatrixResult getMatrix(List<LocationEntity> locations) {
        if (locations == null || locations.isEmpty()) {
            throw new IllegalArgumentException("Locations list must not be empty");
        }

        int size = locations.size();
        MatrixResult result = new MatrixResult(new long[size][size], new int[size][size]);

        try {
            StringBuilder coordinates = new StringBuilder();
            for (LocationEntity loc : locations) {
                if (!coordinates.isEmpty()) {
                    coordinates.append(";");
                }
                coordinates.append(String.format(Locale.US, "%f,%f",
                        loc.getLongitude(), loc.getLatitude()));
            }

            String url = String.format("%s/table/v1/driving/%s?annotations=distance,duration",
                    osrmUrl, coordinates.toString());

            log.debug("Calling OSRM Table API: {}", url);
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);

                JsonNode distances = root.path("distances");
                if (distances.isArray()) {
                    for (int i = 0; i < size; i++) {
                        JsonNode row = distances.get(i);
                        for (int j = 0; j < size; j++) {
                            JsonNode val = row.get(j);
                            if (val == null || val.isNull()) {
                                result.getDistanceMatrix()[i][j] = Long.MAX_VALUE;
                            } else {
                                result.getDistanceMatrix()[i][j] = Math.round(val.asDouble());
                            }
                        }
                    }
                }

                JsonNode durations = root.path("durations");
                if (durations.isArray()) {
                    for (int i = 0; i < size; i++) {
                        JsonNode row = durations.get(i);
                        for (int j = 0; j < size; j++) {
                            JsonNode val = row.get(j);
                            if (val == null || val.isNull()) {
                                result.getDurationMatrix()[i][j] = Integer.MAX_VALUE;
                            } else {
                                result.getDurationMatrix()[i][j] = (int) Math.ceil(val.asDouble() / 60.0);
                            }
                        }
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.error(
                    "OSRM Table API failed (Connection refused or timeout). Falling back to Haversine matrix calculation. Error: {}",
                    e.getMessage());
            return haversineDistanceService.calculateMatrix(locations);
        }
        return haversineDistanceService.calculateMatrix(locations);
    }

    public String getRoutePolyline(List<LocationEntity> waypoints) {
        if (waypoints == null || waypoints.size() < 2) {
            return null;
        }

        try {
            StringBuilder coordinates = new StringBuilder();
            for (LocationEntity loc : waypoints) {
                if (coordinates.length() > 0) {
                    coordinates.append(";");
                }
                coordinates.append(String.format(Locale.US, "%f,%f",
                        loc.getLongitude(), loc.getLatitude()));
            }

            String url = String.format("%s/route/v1/driving/%s?overview=full&geometries=polyline",
                    osrmUrl, coordinates.toString());

            log.debug("Calling OSRM Route API for polyline: {}", url);
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode routes = root.path("routes");
                if (routes.isArray() && routes.size() > 0) {
                    return routes.get(0).path("geometry").asText(null);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get route polyline from OSRM: {}", e.getMessage());
        }
        return null;
    }
}
