package com.logistics.hub.feature.routing.service.impl;

import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.routing.config.RoutingConfig;
import com.logistics.hub.feature.routing.dto.response.DistanceResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service tính khoảng cách địa lý theo đường chim bay
 * Sử dụng công thức Haversine dựa trên kinh độ và vĩ độ
 * 
 * Được sử dụng làm fallback khi OSRM API không khả dụng
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HaversineDistanceService {

    private final RoutingConfig routingConfig;

    /**
     * Tính khoảng cách và thời gian di chuyển giữa hai điểm
     * Sử dụng công thức Haversine để tính khoảng cách đường chim bay
     * Thời gian được ước tính dựa trên tốc độ trung bình cấu hình
     * 
     * @param origin Điểm xuất phát
     * @param destination Điểm đích
     * @return DistanceResult chứa khoảng cách (km) và thời gian (phút)
     * @throws IllegalArgumentException nếu origin hoặc destination là null
     */
    public DistanceResult calculateDistance(LocationEntity origin, LocationEntity destination) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origin and Destination must not be null");
        }

        // Lấy configuration từ application.yml
        double earthRadiusKm = routingConfig.getDistance().getEarthRadiusKm();
        double averageSpeedKmh = routingConfig.getDistance().getAverageSpeedKmh();

        // Convert degrees to radians
        double lat1 = Math.toRadians(origin.getLatitude());
        double lat2 = Math.toRadians(destination.getLatitude());
        double lon1 = Math.toRadians(origin.getLongitude());
        double lon2 = Math.toRadians(destination.getLongitude());

        // Calculate differences
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // Haversine formula
        // a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        // c = 2 ⋅ atan2( √a, √(1−a) )
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // d = R ⋅ c
        double distanceKm = earthRadiusKm * c;

        // Tính thời gian dự kiến dựa trên tốc độ trung bình
        double durationHours = distanceKm / averageSpeedKmh;
        int durationMinutes = (int) Math.ceil(durationHours * 60);

        // Round khoảng cách về 2 chữ số thập phân
        BigDecimal distance = BigDecimal.valueOf(distanceKm).setScale(2, RoundingMode.HALF_UP);

        log.debug("Haversine: {} -> {} = {} km, {} min", 
                 origin.getName(), destination.getName(), distance, durationMinutes);

        // Haversine không có polyline geometry (chỉ là đường thẳng)
        return new DistanceResult(distance, durationMinutes, null);
    }
}
