package com.logistics.hub.feature.routing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "routing")
@Data
public class RoutingConfig {

    private SolverConfig solver = new SolverConfig();
    private DistanceConfig distance = new DistanceConfig();

    @Data
    public static class SolverConfig {
        private int timeLimitSeconds = 5;
        private long vehicleFixedCost = 1_000_000L;
        private int volumeScalingFactor = 1000;
        private int maxTripsPerVehicle = 3;
    }

    @Data
    public static class DistanceConfig {
        private double averageSpeedKmh = 50.0;
        private double earthRadiusKm = 6371.0;
    }
}
