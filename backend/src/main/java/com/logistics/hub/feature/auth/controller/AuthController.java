package com.logistics.hub.feature.auth.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.auth.constant.AuthConstant;
import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.response.AuthTokensResponse;
import com.logistics.hub.feature.auth.dto.response.DispatcherResponse;
import com.logistics.hub.feature.auth.service.AuthService;
import com.logistics.hub.feature.auth.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UrlConstant.Auth.PREFIX)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login & Token APIs for Dispatchers")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String COOKIE_PATH = "/api/v1/auth";

    @PostMapping(UrlConstant.Auth.LOGIN)
    @Operation(summary = "Login with Username/Password", description = "Returns access token in body and sets refresh token as HttpOnly cookie")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        try {
            AuthService.LoginResult result = authService.login(request);
            addRefreshTokenCookie(httpRequest, response, result.refreshToken());
            return ResponseEntity.ok(ApiResponse.success(AuthConstant.LOGIN_SUCCESS, result.response()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        }
    }

    @PostMapping(UrlConstant.Auth.REFRESH)
    @Operation(summary = "Refresh Access Token", description = "Uses refresh token from HttpOnly cookie to issue new tokens")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        try {
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, AuthConstant.REFRESH_TOKEN_NOT_FOUND));
            }
            AuthService.RefreshResult result = authService.refreshToken(refreshToken);
            addRefreshTokenCookie(httpRequest, response, result.refreshToken());
            return ResponseEntity.ok(ApiResponse.success(AuthConstant.TOKEN_REFRESH_SUCCESS, result.response()));
        } catch (RuntimeException e) {
            clearRefreshTokenCookie(httpRequest, response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        }
    }

    @PostMapping(UrlConstant.Auth.LOGOUT)
    @Operation(summary = "Logout", description = "Clears refresh token from cookie and database")
    public ResponseEntity<?> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        clearRefreshTokenCookie(httpRequest, response);
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.LOGOUT_SUCCESS, null));
    }

    @GetMapping(UrlConstant.Auth.ME)
    @Operation(summary = "Get Current User", description = "Returns current authenticated user info (requires valid token)")
    public ResponseEntity<ApiResponse<DispatcherResponse>> getCurrentUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, AuthConstant.NOT_AUTHENTICATED));
        }

        DispatcherResponse userData = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.USER_INFO_RETRIEVED_SUCCESS, userData));
    }

    // ======================== Cookie Helpers ========================

    private void addRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge((int) jwtUtils.getRefreshExpirationSeconds());
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }
}
