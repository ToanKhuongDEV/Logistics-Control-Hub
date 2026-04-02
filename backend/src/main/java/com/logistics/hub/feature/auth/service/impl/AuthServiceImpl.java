package com.logistics.hub.feature.auth.service.impl;

import com.logistics.hub.feature.auth.constant.AuthConstant;
import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.response.AuthTokensResponse;
import com.logistics.hub.feature.auth.dto.response.DispatcherResponse;
import com.logistics.hub.feature.auth.entity.RefreshTokenEntity;
import com.logistics.hub.feature.auth.repository.RefreshTokenRepository;
import com.logistics.hub.feature.auth.service.AuthService;
import com.logistics.hub.feature.auth.util.JwtUtils;
import com.logistics.hub.feature.dispatcher.entity.DispatcherEntity;
import com.logistics.hub.feature.dispatcher.mapper.DispatcherMapper;
import com.logistics.hub.feature.dispatcher.repository.DispatcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final DispatcherRepository dispatcherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final DispatcherMapper dispatcherMapper;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public LoginResult login(LoginRequest request) {
        DispatcherEntity user = dispatcherRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(AuthConstant.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException(AuthConstant.INVALID_CREDENTIALS);
        }

        UserDetails userDetails = new User(user.getUsername(), user.getPassword(), new ArrayList<>());

        // Xóa tất cả refresh token cũ của user
        refreshTokenRepository.deleteByUsername(user.getUsername());

        // Tạo token mới
        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Lưu refresh token vào DB
        saveRefreshToken(refreshToken, user.getUsername());

        return new LoginResult(new AuthTokensResponse(accessToken), refreshToken);
    }

    @Override
    @Transactional
    public RefreshResult refreshToken(String refreshToken) {
        // Extract jti và username từ refresh token
        String jti;
        String username;
        try {
            jti = jwtUtils.extractJtiFromRefreshToken(refreshToken);
            username = jwtUtils.extractUsernameFromRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new RuntimeException(AuthConstant.INVALID_TOKEN);
        }

        // Kiểm tra token có tồn tại trong DB
        RefreshTokenEntity storedToken = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new RuntimeException(AuthConstant.REFRESH_TOKEN_NOT_FOUND));

        // Kiểm tra username khớp
        if (!storedToken.getUsername().equals(username)) {
            throw new RuntimeException(AuthConstant.INVALID_TOKEN);
        }

        // Load user và validate JWT
        DispatcherEntity user = dispatcherRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(AuthConstant.INVALID_TOKEN));

        UserDetails userDetails = new User(user.getUsername(), user.getPassword(), new ArrayList<>());
        if (!jwtUtils.isRefreshTokenValid(refreshToken, userDetails)) {
            // Token hết hạn hoặc invalid → xóa khỏi DB
            refreshTokenRepository.deleteByJti(jti);
            throw new RuntimeException(AuthConstant.INVALID_TOKEN);
        }

        // Token rotation: xóa token cũ, tạo token mới
        refreshTokenRepository.deleteByJti(jti);

        String newAccessToken = jwtUtils.generateToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Lưu refresh token mới vào DB
        saveRefreshToken(newRefreshToken, user.getUsername());

        return new RefreshResult(new AuthTokensResponse(newAccessToken), newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        try {
            String jti = jwtUtils.extractJtiFromRefreshToken(refreshToken);
            refreshTokenRepository.deleteByJti(jti);
        } catch (Exception e) {
            // Token không hợp lệ → bỏ qua, vẫn clear cookie ở controller
        }
    }

    @Override
    public DispatcherResponse getCurrentUser(String username) {
        DispatcherEntity user = dispatcherRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return dispatcherMapper.toResponse(user);
    }

    /**
     * Lưu refresh token mới vào database.
     */
    private void saveRefreshToken(String token, String username) {
        String jti = jwtUtils.extractJtiFromRefreshToken(token);

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setJti(jti);
        entity.setToken(token);
        entity.setUsername(username);
        entity.setExpiresAt(jwtUtils.getRefreshTokenExpirationDate().toInstant());

        refreshTokenRepository.save(entity);
    }
}
