package com.logistics.hub.feature.routing.enums;

/**
 * Nguồn tính toán khoảng cách
 */
public enum DistanceSource {
    /**
     * Tính toán bằng công thức Haversine (as-the-crow-flies)
     */
    CALCULATED,
    
    /**
     * Lấy từ Google Maps API
     */
    GOOGLE_MAPS,
    
    /**
     * Lấy từ OSRM (Open Source Routing Machine)
     */
    OSRM,
    
    /**
     * Lấy từ OpenRouteService
     */
    OPENROUTE,
    
    /**
     * Nhập thủ công
     */
    MANUAL
}
