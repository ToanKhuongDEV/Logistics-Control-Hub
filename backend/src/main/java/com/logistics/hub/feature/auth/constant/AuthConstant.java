package com.logistics.hub.feature.auth.constant;

public final class AuthConstant {
    
    private AuthConstant() {}
    
    public static final String USER_INFO_RETRIEVED_SUCCESS = "User information retrieved successfully";
    public static final String NOT_AUTHENTICATED = "Not authenticated";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String TOKEN_REFRESH_SUCCESS = "Token refreshed successfully";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String INVALID_TOKEN = "Invalid or expired token";
    
    // Validation Messages
    public static final String USERNAME_REQUIRED = "Username cannot be empty";
    public static final String PASSWORD_REQUIRED = "Password cannot be empty";
    public static final String REFRESH_TOKEN_REQUIRED = "Refresh token cannot be empty";
}
