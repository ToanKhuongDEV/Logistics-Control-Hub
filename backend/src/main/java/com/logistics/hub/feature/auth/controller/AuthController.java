package com.logistics.hub.feature.auth.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.base.PaginatedResponse;
import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.auth.constant.AuthConstant;
import com.logistics.hub.feature.auth.dto.request.ChangePasswordRequest;
import com.logistics.hub.feature.auth.dto.request.CreateAccountRequest;
import com.logistics.hub.feature.auth.dto.request.ForgotPasswordRequest;
import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.request.ResetPasswordRequest;
import com.logistics.hub.feature.auth.dto.request.UpdateAccountRequest;
import com.logistics.hub.feature.auth.dto.response.UserResponse;
import com.logistics.hub.feature.auth.service.AuthService;
import com.logistics.hub.feature.auth.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(UrlConstant.Auth.PREFIX)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login & Token APIs for Users")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String ACCESS_COOKIE_PATH = UrlConstant.API_V1;
    private static final String REFRESH_COOKIE_PATH = UrlConstant.Auth.PREFIX;

    @PostMapping(UrlConstant.Auth.LOGIN)
    @Operation(summary = "Login with Username/Password", description = "Sets access and refresh tokens as HttpOnly cookies")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        try {
            AuthService.LoginResult result = authService.login(request);
            addAuthCookies(httpRequest, response, result.accessToken(), result.refreshToken());
            return ResponseEntity.ok(ApiResponse.success(AuthConstant.LOGIN_SUCCESS, null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        }
    }

    @PostMapping(UrlConstant.Auth.REFRESH)
    @Operation(summary = "Refresh Access Token", description = "Uses refresh token from HttpOnly cookie to issue new HttpOnly token cookies")
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
            addAuthCookies(httpRequest, response, result.accessToken(), result.refreshToken());
            return ResponseEntity.ok(ApiResponse.success(AuthConstant.TOKEN_REFRESH_SUCCESS, null));
        } catch (RuntimeException e) {
            clearAuthCookies(httpRequest, response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        }
    }

    @PostMapping(UrlConstant.Auth.LOGOUT)
    @Operation(summary = "Logout", description = "Clears access and refresh token cookies, then removes refresh token from database")
    public ResponseEntity<?> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        clearAuthCookies(httpRequest, response);
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.LOGOUT_SUCCESS, null));
    }

    @GetMapping(UrlConstant.Auth.ME)
    @Operation(summary = "Get Current User", description = "Returns current authenticated user info (requires valid token)")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, AuthConstant.NOT_AUTHENTICATED));
        }

        UserResponse userData = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.USER_INFO_RETRIEVED_SUCCESS, userData));
    }

    @PostMapping(UrlConstant.Auth.CREATE_ACCOUNT)
    @PreAuthorize("hasAuthority('account.manage')")
    @Operation(summary = "Create Employee Account", description = "Creates a new employee account for internal use")
    public ResponseEntity<ApiResponse<UserResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        UserResponse response = authService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), AuthConstant.ACCOUNT_CREATED_SUCCESS, response));
    }

    @GetMapping(UrlConstant.Auth.CREATE_ACCOUNT)
    @PreAuthorize("hasAuthority('account.manage')")
    @Operation(summary = "List employee accounts", description = "Returns paginated employee accounts with dynamic filters")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long depotId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> accounts = authService.getAccounts(pageable, search, role, depotId);

        PaginatedResponse<UserResponse> response = new PaginatedResponse<>();
        response.setData(accounts.getContent());
        response.setPagination(new PaginatedResponse.PaginationInfo(
                accounts.getNumber(),
                accounts.getSize(),
                accounts.getTotalElements(),
                accounts.getTotalPages()
        ));

        return ResponseEntity.ok(ApiResponse.success(AuthConstant.ACCOUNTS_RETRIEVED_SUCCESS, response));
    }

    @GetMapping(UrlConstant.Auth.ACCOUNT_BY_ID)
    @PreAuthorize("hasAuthority('account.manage')")
    @Operation(summary = "Get employee account detail", description = "Returns a single employee account with assigned depots")
    public ResponseEntity<ApiResponse<UserResponse>> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.ACCOUNT_RETRIEVED_SUCCESS, authService.getAccountById(id)));
    }

    @PatchMapping(UrlConstant.Auth.UPDATE_ACCOUNT)
    @PreAuthorize("hasAuthority('account.manage')")
    @Operation(summary = "Update employee account", description = "Updates employee profile, role and assigned depots")
    public ResponseEntity<ApiResponse<UserResponse>> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest request) {
        UserResponse response = authService.updateAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.ACCOUNT_UPDATED_SUCCESS, response));
    }

    @DeleteMapping(UrlConstant.Auth.ACCOUNT_BY_ID)
    @PreAuthorize("hasAuthority('account.manage')")
    @Operation(summary = "Delete employee account", description = "Deletes an employee account and clears assigned depots")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        authService.deleteAccount(id);
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.ACCOUNT_DELETED_SUCCESS, null));
    }

    @PostMapping(UrlConstant.Auth.CHANGE_PASSWORD)
    @Operation(summary = "Change Password", description = "Changes password for the currently authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, AuthConstant.NOT_AUTHENTICATED));
        }

        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.PASSWORD_CHANGED_SUCCESS, null));
    }

    @PostMapping(UrlConstant.Auth.FORGOT_PASSWORD)
    @Operation(summary = "Forgot Password", description = "Sends password reset instructions to user email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.FORGOT_PASSWORD_EMAIL_SENT, null));
    }

    @PostMapping(UrlConstant.Auth.RESET_PASSWORD)
    @Operation(summary = "Reset Password", description = "Resets password using a valid reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(AuthConstant.PASSWORD_RESET_SUCCESS, null));
    }

    private void addAuthCookies(
            HttpServletRequest request,
            HttpServletResponse response,
            String accessToken,
            String refreshToken) {
        addAccessTokenCookie(request, response, accessToken);
        addRefreshTokenCookie(request, response, refreshToken);
    }

    private void addAccessTokenCookie(HttpServletRequest request, HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(ACCESS_COOKIE_PATH);
        cookie.setMaxAge((int) jwtUtils.getJwtExpirationSeconds());
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void addRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(REFRESH_COOKIE_PATH);
        cookie.setMaxAge((int) jwtUtils.getRefreshExpirationSeconds());
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    private void clearAuthCookies(HttpServletRequest request, HttpServletResponse response) {
        clearCookie(request, response, ACCESS_TOKEN_COOKIE, ACCESS_COOKIE_PATH);
        clearCookie(request, response, REFRESH_TOKEN_COOKIE, REFRESH_COOKIE_PATH);
    }

    private void clearCookie(HttpServletRequest request, HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setPath(path);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }
}
