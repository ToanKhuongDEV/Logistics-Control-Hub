package com.logistics.hub.feature.routing.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.routing.service.DistanceService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Service
@Slf4j
public class OsrmDistanceService implements DistanceService {

    @Value("${osrm.url}")
    private String osrmUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BigDecimal getDistanceKm(LocationEntity origin, LocationEntity destination) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origin and Destination must not be null");
        }

        try {
            String coordinates = String.format(Locale.US, "%f,%f;%f,%f",
                    origin.getLongitude(), origin.getLatitude(),
                    destination.getLongitude(), destination.getLatitude());

            String url = String.format("%s/route/v1/driving/%s?overview=false", osrmUrl, coordinates);

            log.debug("Calling OSRM: {}", url);
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode routes = root.path("routes");
                log.info(routes.toString());
                if (routes.isArray() && routes.size() > 0) {
                    double distanceMeters = routes.get(0).path("distance").asDouble();
                    return BigDecimal.valueOf(distanceMeters / 1000.0).setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception e) {
            log.error("Error calling OSRM API", e);
        }
        throw new RuntimeException("Could not calculate distance via OSRM");
    }
}
