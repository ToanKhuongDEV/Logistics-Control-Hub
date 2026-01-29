package com.logistics.hub.common.constant;

public final class SecurityConstant {

    private SecurityConstant() {}

    public static final String UNAUTHORIZED = "Unauthorized access - Authentication required";
    public static final String INVALID_TOKEN = "Invalid or expired token";
    public static final String MISSING_TOKEN = "Missing authentication token";
    
    public static final String FORBIDDEN = "Access denied - Insufficient permissions";
    public static final String INSUFFICIENT_ROLE = "User does not have required role";
}
