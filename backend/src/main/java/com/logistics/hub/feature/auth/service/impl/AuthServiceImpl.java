package com.logistics.hub.feature.auth.service.impl;

import com.logistics.hub.common.exception.UnauthorizedException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.feature.auth.constant.AuthConstant;
import com.logistics.hub.feature.auth.dto.request.ChangePasswordRequest;
import com.logistics.hub.feature.auth.dto.request.CreateAccountRequest;
import com.logistics.hub.feature.auth.dto.request.ForgotPasswordRequest;
import com.logistics.hub.feature.auth.dto.request.LoginRequest;
import com.logistics.hub.feature.auth.dto.request.ResetPasswordRequest;
import com.logistics.hub.feature.auth.dto.request.UpdateAccountRequest;
import com.logistics.hub.feature.auth.dto.response.AssignedDepotResponse;
import com.logistics.hub.feature.auth.dto.response.AuthTokensResponse;
import com.logistics.hub.feature.auth.dto.response.UserResponse;
import com.logistics.hub.feature.auth.entity.PasswordResetTokenEntity;
import com.logistics.hub.feature.auth.entity.RefreshTokenEntity;
import com.logistics.hub.feature.auth.repository.PasswordResetTokenRepository;
import com.logistics.hub.feature.auth.repository.RefreshTokenRepository;
import com.logistics.hub.feature.auth.service.AuthService;
import com.logistics.hub.feature.auth.util.JwtUtils;
import com.logistics.hub.feature.auth.service.PasswordResetMailService;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.user.entity.UserEntity;
import com.logistics.hub.feature.user.mapper.UserMapper;
import com.logistics.hub.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetMailService passwordResetMailService;
    private final DepotRepository depotRepository;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.auth.reset-password-expiration-minutes:15}")
    private long resetPasswordExpirationMinutes;

    @Override
    @Transactional
    public LoginResult login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
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
        UserEntity user = userRepository.findByUsername(username)
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
    public UserResponse getCurrentUser(String username) {
        UserEntity user = userRepository.findByUsernameWithAssignedDepots(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponse response = userMapper.toResponse(user);
        response.setAssignedDepots(toAssignedDepotResponses(user));
        return response;
    }

    @Override
    @Transactional
    public UserResponse createAccount(CreateAccountRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException(AuthConstant.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ValidationException(AuthConstant.EMAIL_ALREADY_EXISTS);
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername().trim());
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(normalizeRole(request.getRole()));

        UserEntity savedUser = userRepository.save(user);
        syncAssignedDepots(savedUser, request.getAssignedDepotIds());
        return getCurrentUserResponse(savedUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAccounts() {
        return userRepository.findAllWithAssignedDepots().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateAccount(Long id, UpdateAccountRequest request) {
        UserEntity user = userRepository.findByIdWithAssignedDepots(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .filter(existingUser -> !existingUser.getId().equals(id))
                .isPresent()) {
            throw new ValidationException(AuthConstant.EMAIL_ALREADY_EXISTS);
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setRole(normalizeRole(request.getRole()));

        userRepository.save(user);
        syncAssignedDepots(user, request.getAssignedDepotIds());
        return getCurrentUserResponse(user.getId());
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException(AuthConstant.NOT_AUTHENTICATED));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException(AuthConstant.CURRENT_PASSWORD_INCORRECT);
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ValidationException(AuthConstant.PASSWORD_MUST_BE_DIFFERENT);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteByUsername(user.getUsername());
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .ifPresent(user -> {
                    passwordResetTokenRepository.deleteByEmailIgnoreCase(user.getEmail());

                    String token = UUID.randomUUID().toString();
                    PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
                    resetToken.setToken(token);
                    resetToken.setEmail(user.getEmail());
                    resetToken.setExpiresAt(Instant.now().plus(resetPasswordExpirationMinutes, ChronoUnit.MINUTES));

                    passwordResetTokenRepository.save(resetToken);

                    String resetUrl = frontendUrl + "/reset-password?token=" + token;
                    passwordResetMailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetUrl);
                });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ValidationException(AuthConstant.INVALID_TOKEN));

        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ValidationException(AuthConstant.INVALID_TOKEN);
        }

        UserEntity user = userRepository.findByEmailIgnoreCase(resetToken.getEmail())
                .orElseThrow(() -> new ValidationException(AuthConstant.INVALID_TOKEN));

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ValidationException(AuthConstant.PASSWORD_MUST_BE_DIFFERENT);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.deleteByUsername(user.getUsername());
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

    private UserResponse getCurrentUserResponse(Long id) {
        UserEntity user = userRepository.findByIdWithAssignedDepots(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(UserEntity user) {
        UserResponse response = userMapper.toResponse(user);
        response.setAssignedDepots(toAssignedDepotResponses(user));
        return response;
    }

    private List<AssignedDepotResponse> toAssignedDepotResponses(UserEntity user) {
        return user.getAssignedDepots().stream()
                .sorted(Comparator.comparing(assignedDepot -> assignedDepot.getId()))
                .map(assignedDepot -> new AssignedDepotResponse(assignedDepot.getId(), assignedDepot.getName()))
                .toList();
    }

    private String normalizeRole(String role) {
        String normalizedRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("ADMIN", "DISPATCHER").contains(normalizedRole)) {
            throw new ValidationException("Role is not supported.");
        }
        return normalizedRole;
    }

    private void syncAssignedDepots(UserEntity user, List<Long> assignedDepotIds) {
        String role = normalizeRole(user.getRole());
        Set<Long> mutableTargetDepotIds = assignedDepotIds == null ? Set.of() : new HashSet<>(assignedDepotIds);

        if ("ADMIN".equals(role)) {
            mutableTargetDepotIds = Set.of();
        }

        Set<Long> targetDepotIds = mutableTargetDepotIds;

        List<DepotEntity> currentDepots = depotRepository.findAll().stream()
                .filter(depot -> depot.getDispatcher() != null && depot.getDispatcher().getId().equals(user.getId()))
                .toList();

        currentDepots.stream()
                .filter(depot -> !targetDepotIds.contains(depot.getId()))
                .forEach(depot -> depot.setDispatcher(null));

        if (!targetDepotIds.isEmpty()) {
            List<DepotEntity> targetDepots = depotRepository.findAllById(targetDepotIds);
            if (targetDepots.size() != targetDepotIds.size()) {
                throw new ValidationException("One or more assigned depots do not exist.");
            }
            targetDepots.forEach(depot -> depot.setDispatcher(user));
            depotRepository.saveAll(targetDepots);
        }

        if (!currentDepots.isEmpty()) {
            depotRepository.saveAll(currentDepots);
        }
    }
}
