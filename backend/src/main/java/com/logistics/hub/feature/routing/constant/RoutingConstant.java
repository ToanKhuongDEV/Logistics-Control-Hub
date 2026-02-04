package com.logistics.hub.feature.routing.constant;

/**
 * Constants cho Routing feature
 * Chứa các message constants cho success và error messages
 */
public final class RoutingConstant {

    private RoutingConstant() {
        // Private constructor để prevent instantiation
    }

    // ==================== Validation Messages ====================
    
    public static final String ORDER_IDS_REQUIRED = "Order IDs are required";
    public static final String ORDER_IDS_EMPTY = "Order IDs list cannot be empty";
    public static final String VEHICLE_IDS_REQUIRED = "Vehicle IDs are required";
    public static final String VEHICLE_IDS_EMPTY = "Vehicle IDs list cannot be empty";
    
    // ==================== Error Messages ====================
    
    public static final String ROUTING_RUN_NOT_FOUND = "Routing run not found with id: ";
    public static final String ORDERS_NOT_FOUND = "Some orders were not found";
    public static final String VEHICLES_NOT_FOUND = "Some vehicles were not found";
    public static final String LOCATIONS_NOT_FOUND = "Not all locations could be found";
    public static final String DEPOT_NOT_ASSIGNED = "Vehicle must have a depot assigned";
    public static final String MULTIPLE_DEPOTS_ERROR = "All vehicles must belong to the same depot";
    public static final String OPTIMIZATION_FAILED = "Route optimization failed - no solution found";
    public static final String OR_TOOLS_LOAD_FAILED = "Failed to load OR-Tools native libraries";
    
    // ==================== Success Messages ====================
    
    public static final String ROUTING_OPTIMIZATION_SUCCESS = "Route optimization completed successfully";
    public static final String ROUTING_RUN_RETRIEVED_SUCCESS = "Routing run retrieved successfully";
    public static final String ROUTING_RUNS_RETRIEVED_SUCCESS = "Routing runs retrieved successfully";
    
    // ==================== Configuration Keys ====================
    
    public static final String CONFIG_OSRM_URL_KEY = "osrm.url";
    public static final String CONFIG_DEFAULT_OSRM_URL = "http://router.project-osrm.org";
}
