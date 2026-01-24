package com.logistics.hub.feature.order.constant;

/**
 * Order constants for validation messages
 */
public final class OrderConstant {
    
    private OrderConstant() {}
    
    public static final String ORDER_CODE_REQUIRED = "Order code is required";
    public static final String DELIVERY_LOCATION_REQUIRED = "Delivery location is required";
    public static final String ORDER_NOT_FOUND = "Order not found with id: ";
    public static final String ORDER_CODE_EXISTS = "Order code already exists: ";
}
