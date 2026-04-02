package com.logistics.hub.feature.auth.service;

import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.request.ChangePasswordRequest;
import com.logistics.hub.feature.auth.dto.request.CreateAccountRequest;
import com.logistics.hub.feature.auth.dto.request.ForgotPasswordRequest;
import com.logistics.hub.feature.auth.dto.request.ResetPasswordRequest;
import com.logistics.hub.feature.auth.dto.response.AuthTokensResponse;
import com.logistics.hub.feature.auth.dto.response.DispatcherResponse;

public interface AuthService {

    /**
     * Kết quả login chứa cả accessToken (body) và refreshToken (cookie).
     */
    record LoginResult(AuthTokensResponse response, String refreshToken) {
    }

    /**
     * Kết quả refresh chứa accessToken mới và refreshToken mới.
     */
    record RefreshResult(AuthTokensResponse response, String refreshToken) {
    }

    LoginResult login(LoginRequest request);

    RefreshResult refreshToken(String refreshToken);

    void logout(String refreshToken);

    DispatcherResponse getCurrentUser(String username);

    DispatcherResponse createAccount(CreateAccountRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
