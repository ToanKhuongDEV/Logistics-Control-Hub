package com.logistics.hub.feature.routing.service.impl;

import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.routing.dto.response.DistanceResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper service để lấy polyline cho toàn bộ route từ OSRM
 * Gọi OSRM với tất cả waypoints trong route để lấy geometry hoàn chỉnh
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoutePolylineService {

    private final OsrmDistanceService osrmDistanceService;

    /**
     * Lấy polyline cho toàn bộ route từ depot → stops → depot
     * 
     * @param routeLocations Danh sách locations theo thứ tự: depot, stop1, stop2,
     *                       ..., depot
     * @return Encoded polyline string hoặc null nếu fail
     */
    public String getRoutePolyline(List<LocationEntity> routeLocations) {
        if (routeLocations == null || routeLocations.size() < 2) {
            return null;
        }

        try {
            // Call OSRM với tất cả waypoints
            // Tạm thời aggregate từng đoạn - có thể optimize sau bằng 1 call OSRM với
            // multi-waypoints
            List<String> segmentPolylines = new ArrayList<>();

            for (int i = 0; i < routeLocations.size() - 1; i++) {
                DistanceResult result = osrmDistanceService.getDistanceWithDuration(
                        routeLocations.get(i),
                        routeLocations.get(i + 1));

                if (result.getPolyline() != null) {
                    segmentPolylines.add(result.getPolyline());
                }
            }

            // Nếu tất cả segments đều có polyline, merge chúng
            // Đơn giản nhất: return polyline đầu tiên (hoặc có thể merge sau)
            if (!segmentPolylines.isEmpty()) {
                // TODO: Implement proper polyline merging if needed
                // For now, return the concatenated polylines separated by |
                return String.join("|", segmentPolylines);
            }

        } catch (Exception e) {
            log.warn("Failed to get route polyline: {}", e.getMessage());
        }

        return null;
    }
}
