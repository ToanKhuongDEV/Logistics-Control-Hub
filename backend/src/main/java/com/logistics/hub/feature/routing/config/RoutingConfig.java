package com.logistics.hub.feature.routing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties cho Routing feature
 * Đọc các settings từ application.yml với prefix "routing"
 */
@Configuration
@ConfigurationProperties(prefix = "routing")
@Data
public class RoutingConfig {

    /**
     * Solver configuration
     */
    private SolverConfig solver = new SolverConfig();

    /**
     * Distance calculation configuration
     */
    private DistanceConfig distance = new DistanceConfig();

    @Data
    public static class SolverConfig {
        /**
         * Thời gian giới hạn cho solver (seconds)
         * Default: 5 giây
         */
        private int timeLimitSeconds = 5;

        /**
         * Fixed cost cho mỗi vehicle (để khuyến khích dùng ít xe)
         * Default: 1,000,000
         */
        private long vehicleFixedCost = 1_000_000L;

        /**
         * Volume scaling factor để convert m³ sang integer
         * Default: 1000 (tức 1 m³ = 1000 units)
         */
        private int volumeScalingFactor = 1000;
    }

    @Data
    public static class DistanceConfig {
        /**
         * Tốc độ trung bình dùng cho Haversine fallback (km/h)
         * Default: 50 km/h
         */
        private double averageSpeedKmh = 50.0;

        /**
         * Bán kính trái đất (km) dùng cho Haversine
         * Default: 6371.0 km
         */
        private double earthRadiusKm = 6371.0;
    }
}
