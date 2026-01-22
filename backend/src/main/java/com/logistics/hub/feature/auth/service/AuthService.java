package com.logistics.hub.feature.auth.service;

import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.request.RefreshTokenRequest;
import com.logistics.hub.feature.auth.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    
    LoginResponse refreshToken(RefreshTokenRequest request);
}

