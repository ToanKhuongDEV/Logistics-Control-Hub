package com.logistics.hub.feature.auth.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.feature.auth.constant.AuthConstant;
import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.request.RefreshTokenRequest;
import com.logistics.hub.feature.auth.dto.response.LoginResponse;
import com.logistics.hub.feature.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login & Token APIs for Dispatchers")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login with Username/Password", description = "Returns access and refresh tokens on successful login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse user = authService.login(request);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Access Token", description = "Use refresh token to get new access token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get Current User", description = "Returns current authenticated user info (requires valid token)")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        // This endpoint requires authentication - will be protected by JWT filter
        // Returns current user info from SecurityContext
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, AuthConstant.NOT_AUTHENTICATED));
        }
        
        Map<String, Object> userData = Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.USER_INFO_RETRIEVED_SUCCESS, userData));
    }
}

