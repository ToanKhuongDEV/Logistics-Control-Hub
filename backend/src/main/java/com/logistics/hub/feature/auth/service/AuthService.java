package com.logistics.hub.feature.auth.service;

import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.request.RefreshTokenRequest;
import com.logistics.hub.feature.auth.dto.response.DispatcherResponse;

public interface AuthService {
    DispatcherResponse login(LoginRequest request);
    
    DispatcherResponse refreshToken(RefreshTokenRequest request);
}
