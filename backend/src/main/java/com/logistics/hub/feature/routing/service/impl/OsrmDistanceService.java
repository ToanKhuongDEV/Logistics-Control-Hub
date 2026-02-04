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
import java.util.Locale;

/**
 * Service tính khoảng cách sử dụng OSRM (Open Source Routing Machine) API
 * Cung cấp khoảng cách và thời gian di chuyển thực tế dựa trên mạng lưới đường bộ
 * 
 * Có cơ chế fallback sang HaversineDistanceService khi OSRM không khả dụng
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OsrmDistanceService implements DistanceService {

    /**
     * URL của OSRM server (có thể là public hoặc self-hosted)
     * Đọc từ application.yml: osrm.url
     */
    @Value("${osrm.url}")
    private String osrmUrl;

    /**
     * HaversineDistanceService dùng làm fallback khi OSRM fail
     */
    private final HaversineDistanceService haversineDistanceService;
    
    /**
     * RestTemplate để gọi OSRM HTTP API
     */
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * ObjectMapper để parse JSON response từ OSRM
     */
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Implementation của DistanceService interface
     * Trả về chỉ khoảng cách, không bao gồm thời gian
     * 
     * @param origin Điểm xuất phát
     * @param destination Điểm đích
     * @return Khoảng cách tính bằng km
     */
    @Override
    public BigDecimal getDistanceKm(LocationEntity origin, LocationEntity destination) {
        DistanceResult result = getDistanceWithDuration(origin, destination);
        return result.getDistanceKm();
    }

    /**
     * Tính khoảng cách VÀ thời gian di chuyển giữa 2 điểm
     * Sử dụng OSRM Route API để lấy thông tin đường đi thực tế
     * 
     * Flow:
     * 1. Gọi OSRM Route API với tọa độ origin và destination
     * 2. Parse JSON response để lấy distance (meters) và duration (seconds)
     * 3. Nếu thất bại (lỗi mạng, server down, không tìm thấy route), fallback sang Haversine
     * 
     * @param origin Điểm xuất phát
     * @param destination Điểm đích
     * @return DistanceResult chứa khoảng cách (km) và thời gian (phút)
     * @throws IllegalArgumentException nếu origin hoặc destination là null
     */
    public DistanceResult getDistanceWithDuration(LocationEntity origin, LocationEntity destination) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origin and Destination must not be null");
        }

        try {
            // Format: longitude,latitude;longitude,latitude
            // OSRM sử dụng thứ tự lon,lat (khác với thông thường)
            String coordinates = String.format(Locale.US, "%f,%f;%f,%f",
                    origin.getLongitude(), origin.getLatitude(),
                    destination.getLongitude(), destination.getLatitude());

            // OSRM Route API endpoint - Changed overview=full to get polyline geometry
            String url = String.format("%s/route/v1/driving/%s?overview=full&geometries=polyline", osrmUrl, coordinates);

            log.debug("Calling OSRM: {}", url);
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode routes = root.path("routes");
                
                if (routes.isArray() && routes.size() > 0) {
                    JsonNode route = routes.get(0);
                    
                    // OSRM trả về distance bằng meters và duration bằng seconds
                    double distanceMeters = route.path("distance").asDouble();
                    double durationSeconds = route.path("duration").asDouble();
                    
                    // Get encoded polyline geometry
                    String polyline = route.path("geometry").asText(null);
                    
                    // Convert sang km và phút
                    BigDecimal distanceKm = BigDecimal.valueOf(distanceMeters / 1000.0)
                            .setScale(2, RoundingMode.HALF_UP);
                    int durationMinutes = (int) Math.ceil(durationSeconds / 60.0);
                    
                    log.debug("OSRM: {} -> {} = {} km, {} min, polyline: {}", 
                             origin.getName(), destination.getName(), distanceKm, durationMinutes, 
                             polyline != null ? "present" : "null");
                    
                    return new DistanceResult(distanceKm, durationMinutes, polyline);
                }
            }
        } catch (Exception e) {
            // Log warning và fallback sang Haversine
            log.warn("OSRM API failed for {} -> {}, falling back to Haversine: {}", 
                    origin.getName(), destination.getName(), e.getMessage());
            return haversineDistanceService.calculateDistance(origin, destination);
        }
        
        // Nếu OSRM không trả về routes (ví dụ: điểm quá xa nhau), dùng Haversine
        log.warn("OSRM returned no routes for {} -> {}, using Haversine fallback", 
                origin.getName(), destination.getName());
        return haversineDistanceService.calculateDistance(origin, destination);
    }


    /**
     * Lấy ma trận khoảng cách giữa N locations sử dụng OSRM Table API
     * Thay vì gọi N² lần Route API, chỉ cần 1 lần gọi Table API
     * 
     * Sử dụng cho routing optimization để giảm số lượng API calls
     * 
     * @param locations Danh sách các locations cần tính ma trận
     * @return MatrixResult chứa:
     *         - distanceMatrix[i][j]: khoảng cách từ location i đến j (meters)
     *         - durationMatrix[i][j]: thời gian từ location i đến j (minutes)
     * @throws IllegalArgumentException nếu locations null hoặc empty
     */
    public MatrixResult getMatrix(java.util.List<LocationEntity> locations) {
        if (locations == null || locations.isEmpty()) {
            throw new IllegalArgumentException("Locations list must not be empty");
        }
        
        int size = locations.size();
        MatrixResult result = new MatrixResult(new long[size][size], new int[size][size]);

        try {
            // 1. Build Coordinates String
            // Format: lon1,lat1;lon2,lat2;lon3,lat3;...
            StringBuilder coordinates = new StringBuilder();
            for (LocationEntity loc : locations) {
                if (!coordinates.isEmpty()) {
                    coordinates.append(";");
                }
                coordinates.append(String.format(Locale.US, "%f,%f", 
                    loc.getLongitude(), loc.getLatitude()));
            }

            // 2. Build URL for OSRM Table API
            // annotations=distance,duration để lấy cả khoảng cách và thời gian
            String url = String.format("%s/table/v1/driving/%s?annotations=distance,duration", 
                    osrmUrl, coordinates.toString());

            log.debug("Calling OSRM Table API: {}", url);
            String response = restTemplate.getForObject(url, String.class);

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                
                // 3. Parse Distances (Meters)
                // OSRM trả về ma trận khoảng cách dạng [[d11,d12,...],[d21,d22,...],...]
                JsonNode distances = root.path("distances");
                if (distances.isArray()) {
                    for (int i = 0; i < size; i++) {
                        JsonNode row = distances.get(i);
                        for (int j = 0; j < size; j++) {
                            JsonNode val = row.get(j);
                             // OSRM trả về null nếu không tìm thấy route
                            if (val == null || val.isNull()) {
                                result.getDistanceMatrix()[i][j] = Long.MAX_VALUE;
                            } else {
                                result.getDistanceMatrix()[i][j] = Math.round(val.asDouble());
                            }
                        }
                    }
                }

                // 4. Parse Durations (Seconds)
                // Convert từ seconds sang minutes
                JsonNode durations = root.path("durations");
                if (durations.isArray()) {
                    for (int i = 0; i < size; i++) {
                        JsonNode row = durations.get(i);
                        for (int j = 0; j < size; j++) {
                            JsonNode val = row.get(j);
                            if (val == null || val.isNull()) {
                                result.getDurationMatrix()[i][j] = Integer.MAX_VALUE;
                            } else {
                                // Convert seconds to minutes (làm tròn lên)
                                result.getDurationMatrix()[i][j] = (int) Math.ceil(val.asDouble() / 60.0);
                            }
                        }
                    }
                }
                return result;
            }
        } catch (Exception e) {
            log.error("OSRM Table API failed", e);
            // Fallback: gọi getDistanceWithDuration cho từng cặp (N² calls)
            log.warn("Falling back to iterative calculation due to Table API failure");
            return calculateMatrixIteratively(locations);
        }
        
        return calculateMatrixIteratively(locations);
    }
    
    /**
     * Fallback method: Tính ma trận khoảng cách bằng cách gọi từng cặp
     * Được sử dụng khi OSRM Table API fail hoặc không khả dụng
     * 
     * Lưu ý: Phương pháp này chậm hơn nhiều (O(N²) API calls) nên chỉ dùng làm fallback
     * 
     * @param locations Danh sách locations cần tính ma trận
     * @return MatrixResult với distance (meters) và duration (minutes)
     */
    private MatrixResult calculateMatrixIteratively(java.util.List<LocationEntity> locations) {
        int size = locations.size();
        MatrixResult result = new MatrixResult(new long[size][size], new int[size][size]);
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    // Khoảng cách từ điểm đến chính nó = 0
                    result.getDistanceMatrix()[i][j] = 0;
                    result.getDurationMatrix()[i][j] = 0;
                } else {
                    // Gọi getDistanceWithDuration (có fallback sang Haversine)
                    DistanceResult dist = getDistanceWithDuration(locations.get(i), locations.get(j));
                    // Convert km về meters để thống nhất với Table API
                    result.getDistanceMatrix()[i][j] = dist.getDistanceKm().multiply(BigDecimal.valueOf(1000)).longValue();
                    result.getDurationMatrix()[i][j] = dist.getDurationMinutes();
                }
            }
        }
        return result;
    }
}
