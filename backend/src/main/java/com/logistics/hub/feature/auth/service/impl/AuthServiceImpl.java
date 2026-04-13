package com.logistics.hub.feature.auth.service.impl;

import com.logistics.hub.common.exception.ForbiddenException;
import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.UnauthorizedException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.audit.constant.AuditAction;
import com.logistics.hub.feature.audit.constant.AuditResourceType;
import com.logistics.hub.feature.audit.constant.AuditStatus;
import com.logistics.hub.feature.audit.service.AuditLogService;
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
import com.logistics.hub.feature.auth.policy.AuthorizationPolicy;
import com.logistics.hub.feature.auth.repository.PasswordResetTokenRepository;
import com.logistics.hub.feature.auth.repository.RefreshTokenRepository;
import com.logistics.hub.feature.auth.service.AuthService;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.auth.service.PasswordResetMailService;
import com.logistics.hub.feature.auth.util.JwtUtils;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.user.entity.UserEntity;
import com.logistics.hub.feature.user.mapper.UserMapper;
import com.logistics.hub.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final AuditLogService auditLogService;
    private final AuthorizationService authorizationService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.auth.reset-password-expiration-minutes:15}")
    private long resetPasswordExpirationMinutes;

    @Override
    @Transactional
    public LoginResult login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    auditLogService.logByUsername(
                            request.getUsername(),
                            null,
                            AuditAction.LOGIN,
                            AuditResourceType.AUTH,
                            request.getUsername(),
                            request.getUsername(),
                            null,
                            AuditStatus.FAILED,
                            AuthConstant.INVALID_CREDENTIALS,
                            null,
                            null,
                            Map.of("username", request.getUsername()));
                    return new RuntimeException(AuthConstant.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditLogService.log(
                    user,
                    AuditAction.LOGIN,
                    AuditResourceType.AUTH,
                    user.getId().toString(),
                    user.getUsername(),
                    firstAssignedDepotId(user),
                    AuditStatus.FAILED,
                    AuthConstant.INVALID_CREDENTIALS,
                    null,
                    null,
                    Map.of("username", user.getUsername()));
            throw new RuntimeException(AuthConstant.INVALID_CREDENTIALS);
        }

        UserDetails userDetails = new User(user.getUsername(), user.getPassword(), new ArrayList<>());
        refreshTokenRepository.deleteByUsername(user.getUsername());

        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        saveRefreshToken(refreshToken, user.getUsername());

        auditLogService.log(
                user,
                AuditAction.LOGIN,
                AuditResourceType.AUTH,
                user.getId().toString(),
                user.getUsername(),
                firstAssignedDepotId(user),
                AuditStatus.SUCCESS,
                AuthConstant.LOGIN_SUCCESS,
                null,
                null,
                Map.of("username", user.getUsername()));

        return new LoginResult(new AuthTokensResponse(accessToken), refreshToken);
    }

    @Override
    @Transactional
    public RefreshResult refreshToken(String refreshToken) {
        String jti;
        String username;
        try {
            jti = jwtUtils.extractJtiFromRefreshToken(refreshToken);
            username = jwtUtils.extractUsernameFromRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new RuntimeException(AuthConstant.INVALID_TOKEN);
        }

        RefreshTokenEntity storedToken = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new RuntimeException(AuthConstant.REFRESH_TOKEN_NOT_FOUND));

        if (!storedToken.getUsername().equals(username)) {
            throw new RuntimeException(AuthConstant.INVALID_TOKEN);
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(AuthConstant.INVALID_TOKEN));

        UserDetails userDetails = new User(user.getUsername(), user.getPassword(), new ArrayList<>());
        if (!jwtUtils.isRefreshTokenValid(refreshToken, userDetails)) {
            refreshTokenRepository.deleteByJti(jti);
            throw new RuntimeException(AuthConstant.INVALID_TOKEN);
        }

        refreshTokenRepository.deleteByJti(jti);

        String newAccessToken = jwtUtils.generateToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);
        saveRefreshToken(newRefreshToken, user.getUsername());

        return new RefreshResult(new AuthTokensResponse(newAccessToken), newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        try {
            String jti = jwtUtils.extractJtiFromRefreshToken(refreshToken);
            String username = jwtUtils.extractUsernameFromRefreshToken(refreshToken);
            refreshTokenRepository.deleteByJti(jti);
            userRepository.findByUsernameWithAssignedDepots(username).ifPresent(user -> auditLogService.log(
                    user,
                    AuditAction.LOGOUT,
                    AuditResourceType.AUTH,
                    user.getId().toString(),
                    user.getUsername(),
                    firstAssignedDepotId(user),
                    AuditStatus.SUCCESS,
                    AuthConstant.LOGOUT_SUCCESS,
                    null,
                    null,
                    Map.of("username", user.getUsername())));
        } catch (Exception ignored) {
        }
    }

    @Override
    public UserResponse getCurrentUser(String username) {
        UserEntity user = userRepository.findByUsernameWithAssignedDepots(username)
                .orElseThrow(() -> new RuntimeException(AuthConstant.ACCOUNT_NOT_FOUND));

        UserResponse response = userMapper.toResponse(user);
        response.setPermissions(new ArrayList<>(AuthorizationPolicy.permissionsForRole(user.getRole())));
        response.setAssignedDepots(toAssignedDepotResponses(user));
        return response;
    }

    @Override
    @Transactional
    public UserResponse createAccount(CreateAccountRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername().trim())) {
                throw new ValidationException(AuthConstant.USERNAME_ALREADY_EXISTS);
            }

            if (userRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
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
            UserEntity createdUser = userRepository.findByIdWithAssignedDepots(savedUser.getId()).orElse(savedUser);

            auditLogService.log(
                    getCurrentActorForAudit(),
                    AuditAction.CREATE,
                    AuditResourceType.USER,
                    createdUser.getId().toString(),
                    createdUser.getUsername(),
                    firstAssignedDepotId(createdUser),
                    AuditStatus.SUCCESS,
                    "Created employee account",
                    null,
                    userAuditSnapshot(createdUser),
                    Map.of("role", createdUser.getRole()));

            return getCurrentUserResponse(savedUser.getId());
        } catch (RuntimeException ex) {
            logUserFailure(AuditAction.CREATE, null, request.getUsername(), null, accountRequestSnapshot(request), ex);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAccounts(Pageable pageable, String search, String role, Long depotId) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ACCOUNT_MANAGE);
        return userRepository.searchAccounts(normalizeSearch(search), normalizeRoleFilter(role), depotId, pageable)
                .map(this::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getAccountById(Long id) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ACCOUNT_MANAGE);
        return getCurrentUserResponse(id);
    }

    @Override
    @Transactional
    public UserResponse updateAccount(Long id, UpdateAccountRequest request) {
        UserEntity user = null;
        Map<String, Object> beforeData = null;
        try {
            UserEntity actor = authorizationService.getCurrentUser();
            if (!AuthorizationPolicy.permissionsForRole(actor.getRole())
                    .contains(AuthorizationPolicy.PERMISSION_ACCOUNT_MANAGE)) {
                throw new ForbiddenException("Bạn không có quyền thực hiện thao tác này.");
            }
            user = userRepository.findByIdWithAssignedDepots(id)
                    .orElseThrow(() -> new ResourceNotFoundException(AuthConstant.ACCOUNT_NOT_FOUND));
            validateAdminAccountManagement(actor, user);
            beforeData = userAuditSnapshot(user);

            boolean fullNameProvided = request.getFullName() != null;
            boolean emailProvided = request.getEmail() != null;
            boolean roleProvided = request.getRole() != null;
            boolean assignedDepotsProvided = request.getAssignedDepotIds() != null;

            if (!fullNameProvided && !emailProvided && !roleProvided && !assignedDepotsProvided) {
                throw new ValidationException("No fields provided for update.");
            }

            boolean userChanged = false;
            boolean roleChanged = false;
            boolean depotAssignmentsChanged = false;

            if (fullNameProvided) {
                String fullName = request.getFullName().trim();
                if (fullName.isEmpty()) {
                    throw new ValidationException("Ho ten khong duoc de trong");
                }
                if (!fullName.equals(user.getFullName())) {
                    user.setFullName(fullName);
                    userChanged = true;
                }
            }

            if (emailProvided) {
                String normalizedEmail = request.getEmail().trim().toLowerCase();
                if (normalizedEmail.isEmpty()) {
                    throw new ValidationException("Email khong duoc de trong");
                }

                if (!normalizedEmail.equalsIgnoreCase(user.getEmail())
                        && userRepository.findByEmailIgnoreCase(normalizedEmail)
                                .filter(existingUser -> !existingUser.getId().equals(id))
                                .isPresent()) {
                    throw new ValidationException(AuthConstant.EMAIL_ALREADY_EXISTS);
                }

                if (!normalizedEmail.equalsIgnoreCase(user.getEmail())) {
                    user.setEmail(normalizedEmail);
                    userChanged = true;
                }
            }

            String currentRole = normalizeRole(user.getRole());
            String targetRole = currentRole;
            if (roleProvided) {
                String requestedRole = request.getRole().trim();
                if (requestedRole.isEmpty()) {
                    throw new ValidationException("Vai tro khong duoc de trong");
                }

                targetRole = normalizeRole(requestedRole);
                validateAdminRetention(user, targetRole);

                if (!targetRole.equals(currentRole)) {
                    user.setRole(targetRole);
                    userChanged = true;
                    roleChanged = true;
                }
            }

            Set<Long> currentAssignedDepotIds = assignedDepotIdsOf(user);
            Set<Long> requestedAssignedDepotIds = assignedDepotsProvided
                    ? new HashSet<>(request.getAssignedDepotIds())
                    : currentAssignedDepotIds;

            if (assignedDepotsProvided) {
                depotAssignmentsChanged = !requestedAssignedDepotIds.equals(currentAssignedDepotIds);
            }

            if (userChanged) {
                userRepository.save(user);
            }

            if (roleChanged || depotAssignmentsChanged) {
                syncAssignedDepots(user, new ArrayList<>(requestedAssignedDepotIds));
            }

            UserEntity updatedUser = userRepository.findByIdWithAssignedDepots(user.getId()).orElse(user);

            auditLogService.log(
                    getCurrentActorForAudit(),
                    AuditAction.UPDATE,
                    AuditResourceType.USER,
                    updatedUser.getId().toString(),
                    updatedUser.getUsername(),
                    firstAssignedDepotId(updatedUser),
                    AuditStatus.SUCCESS,
                    "Updated employee account",
                    beforeData,
                    userAuditSnapshot(updatedUser),
                    Map.of(
                            "role", updatedUser.getRole(),
                            "updatedFields", updatedAccountFields(request)));

            return getCurrentUserResponse(user.getId());
        } catch (RuntimeException ex) {
            logUserFailure(AuditAction.UPDATE, String.valueOf(id), user != null ? user.getUsername() : null, beforeData,
                    updateAccountRequestSnapshot(request), ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        UserEntity user = null;
        try {
            authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_ACCOUNT_MANAGE);
            user = userRepository.findByIdWithAssignedDepots(id)
                    .orElseThrow(() -> new ResourceNotFoundException(AuthConstant.ACCOUNT_NOT_FOUND));

            UserEntity actor = authorizationService.getCurrentUser();
            validateAdminAccountManagement(actor, user);
            if (actor.getId().equals(id)) {
                throw new ValidationException(AuthConstant.CANNOT_DELETE_OWN_ACCOUNT);
            }

            validateAdminRetention(user, null);

            Map<String, Object> beforeData = userAuditSnapshot(user);
            clearAssignedDepots(user);
            refreshTokenRepository.deleteByUsername(user.getUsername());
            passwordResetTokenRepository.deleteByEmailIgnoreCase(user.getEmail());
            userRepository.delete(user);

            auditLogService.log(
                    actor,
                    AuditAction.DELETE,
                    AuditResourceType.USER,
                    user.getId().toString(),
                    user.getUsername(),
                    null,
                    AuditStatus.SUCCESS,
                    "Deleted employee account",
                    beforeData,
                    null,
                    Map.of("role", user.getRole()));
        } catch (RuntimeException ex) {
            logUserFailure(AuditAction.DELETE, String.valueOf(id), user != null ? user.getUsername() : null,
                    user != null ? userAuditSnapshot(user) : null, null, ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        UserEntity user = null;
        try {
            user = userRepository.findByUsername(username)
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
            auditLogService.log(
                    user,
                    AuditAction.CHANGE_PASSWORD,
                    AuditResourceType.AUTH,
                    user.getId().toString(),
                    user.getUsername(),
                    firstAssignedDepotId(user),
                    AuditStatus.SUCCESS,
                    AuthConstant.PASSWORD_CHANGED_SUCCESS,
                    null,
                    null,
                    Map.of("username", user.getUsername()));
        } catch (RuntimeException ex) {
            if (user != null && (ex instanceof ValidationException || ex instanceof UnauthorizedException)) {
                auditLogService.log(
                        user,
                        AuditAction.CHANGE_PASSWORD,
                        AuditResourceType.AUTH,
                        user.getId().toString(),
                        user.getUsername(),
                        firstAssignedDepotId(user),
                        AuditStatus.FAILED,
                        ex.getMessage(),
                        null,
                        null,
                        Map.of("exceptionType", ex.getClass().getSimpleName(), "username", username));
            }
            throw ex;
        }
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
                .orElseThrow(() -> new ResourceNotFoundException(AuthConstant.ACCOUNT_NOT_FOUND));
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(UserEntity user) {
        UserResponse response = userMapper.toResponse(user);
        response.setPermissions(new ArrayList<>(AuthorizationPolicy.permissionsForRole(user.getRole())));
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
        return AuthorizationPolicy.normalizeRole(role);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return "%" + search.trim().toLowerCase() + "%";
    }

    private String normalizeRoleFilter(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }
        return normalizeRole(role);
    }

    private void syncAssignedDepots(UserEntity user, List<Long> assignedDepotIds) {
        String role = normalizeRole(user.getRole());
        Set<Long> mutableTargetDepotIds = assignedDepotIds == null ? Set.of() : new HashSet<>(assignedDepotIds);

        if (AuthorizationPolicy.ROLE_DISPATCHER.equals(role)) {
            if (mutableTargetDepotIds.isEmpty()) {
                throw new ValidationException("Dispatcher must be assigned at least one depot.");
            }
        } else {
            // ADMIN and USER do not require scoped depot assignments
            mutableTargetDepotIds = Set.of();
        }

        Set<Long> targetDepotIds = mutableTargetDepotIds;

        List<DepotEntity> currentDepots = depotRepository.findByDispatcher_Id(user.getId());

        currentDepots.stream()
                .filter(depot -> !targetDepotIds.contains(depot.getId()))
                .forEach(depot -> depot.setDispatcher(null));

        if (!targetDepotIds.isEmpty()) {
            List<DepotEntity> targetDepots = depotRepository.findAllById(targetDepotIds);
            if (targetDepots.size() != targetDepotIds.size()) {
                throw new ValidationException("One or more assigned depots do not exist.");
            }
            boolean hasConflictingDispatcher = targetDepots.stream()
                    .map(DepotEntity::getDispatcher)
                    .anyMatch(dispatcher -> dispatcher != null && !dispatcher.getId().equals(user.getId()));
            if (hasConflictingDispatcher) {
                throw new ValidationException("One or more assigned depots already belong to another dispatcher.");
            }
            targetDepots.forEach(depot -> depot.setDispatcher(user));
            depotRepository.saveAll(targetDepots);
        }

        if (!currentDepots.isEmpty()) {
            depotRepository.saveAll(currentDepots);
        }
    }

    private void clearAssignedDepots(UserEntity user) {
        List<DepotEntity> currentDepots = depotRepository.findByDispatcher_Id(user.getId());

        if (currentDepots.isEmpty()) {
            return;
        }

        currentDepots.forEach(depot -> depot.setDispatcher(null));
        depotRepository.saveAll(currentDepots);
    }

    private void validateAdminRetention(UserEntity user, String nextRole) {
        String currentRole = normalizeRole(user.getRole());
        String targetRole = nextRole == null ? null : normalizeRole(nextRole);

        boolean removingAdminRole = AuthorizationPolicy.ROLE_ADMIN.equals(currentRole)
                && (targetRole == null || !AuthorizationPolicy.ROLE_ADMIN.equals(targetRole));

        if (removingAdminRole && userRepository.countByRoleIgnoreCase(AuthorizationPolicy.ROLE_ADMIN) <= 1) {
            throw new ValidationException(AuthConstant.LAST_ADMIN_REQUIRED);
        }
    }

    private void validateAdminAccountManagement(UserEntity actor, UserEntity targetUser) {
        String actorRole = normalizeRole(actor.getRole());
        String targetRole = normalizeRole(targetUser.getRole());

        boolean isManagingAnotherAdmin = AuthorizationPolicy.ROLE_ADMIN.equals(actorRole)
                && AuthorizationPolicy.ROLE_ADMIN.equals(targetRole)
                && !actor.getId().equals(targetUser.getId());

        if (isManagingAnotherAdmin) {
            throw new ForbiddenException(AuthConstant.CANNOT_MANAGE_OTHER_ADMIN_ACCOUNT);
        }
    }

    private UserEntity getCurrentActorForAudit() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsernameWithAssignedDepots(authentication.getName()).orElse(null);
    }

    private Long firstAssignedDepotId(UserEntity user) {
        return user.getAssignedDepots().stream()
                .map(DepotEntity::getId)
                .sorted()
                .findFirst()
                .orElse(null);
    }

    private Map<String, Object> userAuditSnapshot(UserEntity user) {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", user.getId());
        snapshot.put("username", user.getUsername());
        snapshot.put("fullName", user.getFullName());
        snapshot.put("email", user.getEmail());
        snapshot.put("role", user.getRole());
        snapshot.put("assignedDepotIds", user.getAssignedDepots().stream()
                .map(DepotEntity::getId)
                .sorted()
                .toList());
        return snapshot;
    }

    private Map<String, Object> accountRequestSnapshot(CreateAccountRequest request) {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("username", request.getUsername());
        snapshot.put("fullName", request.getFullName());
        snapshot.put("email", request.getEmail());
        snapshot.put("role", request.getRole());
        snapshot.put("assignedDepotIds", request.getAssignedDepotIds());
        return snapshot;
    }

    private Map<String, Object> updateAccountRequestSnapshot(UpdateAccountRequest request) {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("fullName", request.getFullName());
        snapshot.put("email", request.getEmail());
        snapshot.put("role", request.getRole());
        snapshot.put("assignedDepotIds", request.getAssignedDepotIds());
        return snapshot;
    }

    private Set<Long> assignedDepotIdsOf(UserEntity user) {
        return user.getAssignedDepots().stream()
                .map(DepotEntity::getId)
                .collect(Collectors.toSet());
    }

    private List<String> updatedAccountFields(UpdateAccountRequest request) {
        List<String> fields = new ArrayList<>();
        if (request.getFullName() != null) {
            fields.add("fullName");
        }
        if (request.getEmail() != null) {
            fields.add("email");
        }
        if (request.getRole() != null) {
            fields.add("role");
        }
        if (request.getAssignedDepotIds() != null) {
            fields.add("assignedDepotIds");
        }
        return fields;
    }

    private void logUserFailure(String action, String resourceId, String resourceName, Object beforeData,
            Object afterData, RuntimeException ex) {
        if (!(ex instanceof ValidationException || ex instanceof ForbiddenException
                || ex instanceof ResourceNotFoundException)) {
            return;
        }

        auditLogService.log(
                getCurrentActorForAudit(),
                action,
                AuditResourceType.USER,
                resourceId,
                resourceName,
                null,
                AuditStatus.FAILED,
                ex.getMessage(),
                beforeData,
                afterData,
                Map.of("exceptionType", ex.getClass().getSimpleName()));
    }
}
