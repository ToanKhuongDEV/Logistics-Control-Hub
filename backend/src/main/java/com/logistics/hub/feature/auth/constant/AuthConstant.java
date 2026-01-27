package com.logistics.hub.feature.auth.constant;

/**
 * Authentication constants for messages
 */
public final class AuthConstant {
    
    private AuthConstant() {}
    
    // API Response Messages
    public static final String USER_INFO_RETRIEVED_SUCCESS = "User information retrieved successfully";
    public static final String NOT_AUTHENTICATED = "Not authenticated";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String TOKEN_REFRESH_SUCCESS = "Token refreshed successfully";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String INVALID_TOKEN = "Invalid or expired token";
}
