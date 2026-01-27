package com.logistics.hub.feature.auth.service.impl;

import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.request.RefreshTokenRequest;
import com.logistics.hub.feature.auth.dto.response.LoginResponse;
import com.logistics.hub.feature.dispatcher.entity.DispatcherEntity;
import com.logistics.hub.feature.dispatcher.repository.DispatcherRepository;
import com.logistics.hub.feature.auth.service.AuthService;
import com.logistics.hub.feature.auth.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final DispatcherRepository dispatcherRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. Find user by username
        DispatcherEntity user = dispatcherRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));



        // 3. Verify password using BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // 4. Generate Tokens
        UserDetails userDetails = new User(user.getUsername(), user.getPassword(), new ArrayList<>());
        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        // 5. Return Response
        return new LoginResponse(accessToken, refreshToken);
    }


    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // 1. Extract username from refresh token
        String username;
        try {
            username = jwtUtils.extractUsernameFromRefreshToken(request.getRefreshToken());
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 2. Find user in database
        DispatcherEntity user = dispatcherRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));



        // 4. Validate refresh token
        UserDetails userDetails = new User(user.getUsername(), user.getPassword(), new ArrayList<>());
        if (!jwtUtils.isRefreshTokenValid(request.getRefreshToken(), userDetails)) {
            throw new RuntimeException("Refresh token expired or invalid");
        }

        // 5. Generate new tokens
        String newAccessToken = jwtUtils.generateToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        // 6. Return Response with new tokens
        return new LoginResponse(newAccessToken, newRefreshToken);
    }

}
