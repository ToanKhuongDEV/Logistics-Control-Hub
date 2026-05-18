package com.logistics.hub.feature.auth.service;

import com.logistics.hub.feature.auth.dto.request.ChangePasswordRequest;
import com.logistics.hub.feature.auth.dto.request.CreateAccountRequest;
import com.logistics.hub.feature.auth.dto.request.ForgotPasswordRequest;
import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.request.ResetPasswordRequest;
import com.logistics.hub.feature.auth.dto.request.UpdateAccountRequest;
import com.logistics.hub.feature.auth.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthService {

    record LoginResult(String accessToken, String refreshToken) {
    }

    record RefreshResult(String accessToken, String refreshToken) {
    }

    LoginResult login(LoginRequest request);

    RefreshResult refreshToken(String refreshToken);

    void logout(String refreshToken);

    UserResponse getCurrentUser(String username);

    UserResponse createAccount(CreateAccountRequest request);

    Page<UserResponse> getAccounts(Pageable pageable, String search, String role, Long depotId);

    UserResponse getAccountById(Long id);

    UserResponse updateAccount(Long id, UpdateAccountRequest request);

    void deleteAccount(Long id);

    void changePassword(String username, ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
