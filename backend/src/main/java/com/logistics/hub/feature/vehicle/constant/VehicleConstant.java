package com.logistics.hub.feature.vehicle.constant;

public final class VehicleConstant {
    
    private VehicleConstant() {}
    
    public static final String VEHICLE_CODE_REQUIRED = "Vehicle code is required";
    public static final String VEHICLE_STATUS_REQUIRED = "Vehicle status is required";
    public static final String VEHICLE_CODE_EXISTS = "Vehicle code already exists: ";
    public static final String VEHICLE_NOT_FOUND = "Vehicle not found with id: ";
    public static final String VEHICLE_IN_USE = "Vehicle is assigned to active routes and cannot be deleted";
    
    public static final String VEHICLE_RETRIEVED_SUCCESS = "Vehicle retrieved successfully";
    public static final String VEHICLES_RETRIEVED_SUCCESS = "Vehicles retrieved successfully";
    public static final String VEHICLE_CREATED_SUCCESS = "Vehicle created successfully";
    public static final String VEHICLE_UPDATED_SUCCESS = "Vehicle updated successfully";
    public static final String VEHICLE_DELETED_SUCCESS = "Vehicle deleted successfully";
    public static final String VEHICLE_STATISTICS_RETRIEVED_SUCCESS = "Vehicle statistics retrieved successfully";
    
    public static final String MAX_WEIGHT_POSITIVE = "Max weight must be positive";
    public static final String MAX_VOLUME_POSITIVE = "Max volume must be positive";
    public static final String COST_PER_KM_POSITIVE = "Cost per km must be positive";
}
