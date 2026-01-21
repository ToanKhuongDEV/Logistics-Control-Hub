package com.logistics.hub.feature.routing.enums;

/**
 * Distance calculation source enumeration
 */
public enum DistanceSource {
    /**
     * Calculated using Haversine formula (as-the-crow-flies)
     */
    CALCULATED,
    
    /**
     * From Google Maps API
     */
    GOOGLE_MAPS,
    
    /**
     * From OSRM (Open Source Routing Machine)
     */
    OSRM,
    
    /**
     * From OpenRouteService
     */
    OPENROUTE,
    
    /**
     * Manual input
     */
    MANUAL
}

