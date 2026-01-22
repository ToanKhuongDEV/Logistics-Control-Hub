package com.logistics.hub.feature.order.constant;

public class OrderConstant {
    public static final String ENTITY_NAME = "DeliveryOrder";
    public static final String ORDER_NOT_FOUND = "Order not found with id: "; // Keeping for backward compat if needed, but discouraged
    public static final String ORDER_NUMBER_EXISTS = "Order number already exists: ";
    public static final String ORDER_NUMBER_REQUIRED = "Order number is required";
    public static final String CUSTOMER_ID_REQUIRED = "Customer ID is required";
    public static final String PICKUP_LOCATION_REQUIRED = "Pickup location is required";
    public static final String DELIVERY_LOCATION_REQUIRED = "Delivery location is required";
    public static final String DELIVERY_TIME_REQUIRED = "Delivery time window is required";
    public static final String WEIGHT_REQUIRED = "Weight is required";
    public static final String PRIORITY_REQUIRED = "Priority is required";
    public static final String STATUS_REQUIRED = "Order status is required";

    private OrderConstant() {
        // Prevent instantiation
    }
}
