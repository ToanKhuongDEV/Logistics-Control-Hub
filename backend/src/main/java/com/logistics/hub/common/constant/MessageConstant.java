package com.logistics.hub.common.constant;

public class MessageConstant {
    public static final String SUCCESS = "Success";
    public static final String ERROR_NOT_FOUND = "Resource not found";
    public static final String ERROR_INTERNAL_SERVER = "Internal server error";
    public static final String ERROR_BAD_REQUEST = "Bad request";

    // Value Object Validations
    public static final String LATITUDE_INVALID = "Latitude must be between -90 and 90";
    public static final String LONGITUDE_INVALID = "Longitude must be between -180 and 180";
    public static final String START_TIME_REQUIRED = "Start time is required";
    public static final String END_TIME_REQUIRED = "End time is required";
    public static final String LOCATION_REQUIRED = "Locations cannot be null";
    public static final String COORDINATES_REQUIRED = "Location coordinates cannot be null";

    private MessageConstant() {
        // Prevent instantiation
    }
}
