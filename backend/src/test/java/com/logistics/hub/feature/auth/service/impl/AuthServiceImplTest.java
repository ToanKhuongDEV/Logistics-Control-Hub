package com.logistics.hub.feature.auth.service.impl;

import com.logistics.hub.common.exception.ForbiddenException;
import com.logistics.hub.feature.audit.service.AuditLogService;
import com.logistics.hub.feature.auth.constant.AuthConstant;
import com.logistics.hub.feature.auth.dto.request.UpdateAccountRequest;
import com.logistics.hub.feature.auth.repository.PasswordResetTokenRepository;
import com.logistics.hub.feature.auth.repository.RefreshTokenRepository;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.auth.service.PasswordResetMailService;
import com.logistics.hub.feature.auth.util.JwtUtils;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.user.entity.UserEntity;
import com.logistics.hub.feature.user.mapper.UserMapper;
import com.logistics.hub.feature.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordResetMailService passwordResetMailService;

    @Mock
    private DepotRepository depotRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void updateAccount_shouldRejectWhenAdminUpdatesAnotherAdmin() {
        UserEntity actor = adminUser(1L, "admin01");
        UserEntity target = adminUser(2L, "admin02");
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setFullName("Admin Two");
        request.setEmail("admin02@example.com");
        request.setRole("ADMIN");
        request.setAssignedDepotIds(List.of());

        doNothing().when(authorizationService).requirePermission("account.manage");
        when(authorizationService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findByIdWithAssignedDepots(2L)).thenReturn(Optional.of(target));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> authService.updateAccount(2L, request));

        assertEquals(AuthConstant.CANNOT_MANAGE_OTHER_ADMIN_ACCOUNT, ex.getMessage());
        verify(userRepository, never()).save(target);
    }

    @Test
    void deleteAccount_shouldRejectWhenAdminDeletesAnotherAdmin() {
        UserEntity actor = adminUser(1L, "admin01");
        UserEntity target = adminUser(2L, "admin02");

        doNothing().when(authorizationService).requirePermission("account.manage");
        when(authorizationService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findByIdWithAssignedDepots(2L)).thenReturn(Optional.of(target));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> authService.deleteAccount(2L));

        assertEquals(AuthConstant.CANNOT_MANAGE_OTHER_ADMIN_ACCOUNT, ex.getMessage());
        verify(userRepository, never()).delete(target);
    }

    private UserEntity adminUser(Long id, String username) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setFullName(username);
        user.setEmail(username + "@example.com");
        user.setRole("ADMIN");
        return user;
    }
}
