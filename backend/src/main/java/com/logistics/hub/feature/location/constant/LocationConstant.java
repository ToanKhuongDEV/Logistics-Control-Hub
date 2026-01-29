package com.logistics.hub.feature.location.constant;

public final class LocationConstant {

    private LocationConstant() {}

    public static final String LOCATION_NAME_REQUIRED = "Location name/address is required";
    public static final String LATITUDE_RANGE = "Latitude must be between -90 and 90";
    public static final String LONGITUDE_RANGE = "Longitude must be between -180 and 180";
    public static final String LATITUDE_REQUIRED = "Latitude is required";
    public static final String LONGITUDE_REQUIRED = "Longitude is required";
    public static final String STREET_REQUIRED = "Street is required";
    public static final String CITY_REQUIRED = "City is required";
    public static final String COUNTRY_REQUIRED = "Country is required";

    public static final String LOCATION_CREATED_SUCCESS = "Location created successfully";
    public static final String LOCATION_DELETED_SUCCESS = "Location deleted successfully";

    public static final String GEOCODE_ERROR = "Could not geocode address: ";
    public static final String LOCATION_NOT_FOUND = "Location not found with id: ";
    public static final String LOCATION_RETRIEVAL_ERROR = "Location exists but cannot be retrieved";
}
