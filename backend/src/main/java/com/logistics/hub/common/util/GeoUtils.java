package com.logistics.hub.common.util;

import com.logistics.hub.common.constant.MessageConstant;
import com.logistics.hub.common.valueobject.Location;

public class GeoUtils {

    private static final int EARTH_RADIUS_KM = 6371;

    private GeoUtils() {
        // Prevent instantiation
    }

    /**
     * Calculate distance between two locations using Haversine formula
     * @param loc1 First location
     * @param loc2 Second location
     * @return Distance in kilometers
     */
    public static double calculateDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) {
            throw new IllegalArgumentException(MessageConstant.LOCATION_REQUIRED);
        }
        
        // Handle null coordinates gracefully if needed, or assume Validated Location
        if (loc1.getLatitude() == null || loc1.getLongitude() == null ||
            loc2.getLatitude() == null || loc2.getLongitude() == null) {
            throw new IllegalArgumentException(MessageConstant.COORDINATES_REQUIRED);
        }

        double lat1Rad = Math.toRadians(loc1.getLatitude());
        double lat2Rad = Math.toRadians(loc2.getLatitude());
        double deltaLat = Math.toRadians(loc2.getLatitude() - loc1.getLatitude());
        double deltaLon = Math.toRadians(loc2.getLongitude() - loc1.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
