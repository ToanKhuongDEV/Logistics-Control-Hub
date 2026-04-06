package com.logistics.hub.feature.auth.service;

import com.logistics.hub.feature.auth.dto.request.ChangePasswordRequest;
import com.logistics.hub.feature.auth.dto.request.CreateAccountRequest;
import com.logistics.hub.feature.auth.dto.request.ForgotPasswordRequest;
import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.request.ResetPasswordRequest;
import com.logistics.hub.feature.auth.dto.request.UpdateAccountRequest;
import com.logistics.hub.feature.auth.dto.response.AuthTokensResponse;
import com.logistics.hub.feature.auth.dto.response.UserResponse;

import java.util.List;

public interface AuthService {

    record LoginResult(AuthTokensResponse response, String refreshToken) {
    }

    record RefreshResult(AuthTokensResponse response, String refreshToken) {
    }

    LoginResult login(LoginRequest request);

    RefreshResult refreshToken(String refreshToken);

    void logout(String refreshToken);

    UserResponse getCurrentUser(String username);

    UserResponse createAccount(CreateAccountRequest request);

    List<UserResponse> getAccounts();

    UserResponse updateAccount(Long id, UpdateAccountRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
