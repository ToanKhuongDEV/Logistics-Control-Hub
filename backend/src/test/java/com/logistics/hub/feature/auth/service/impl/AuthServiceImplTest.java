package com.logistics.hub.feature.auth.service.impl;

import com.logistics.hub.common.exception.ForbiddenException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.audit.service.AuditLogService;
import com.logistics.hub.feature.auth.constant.AuthConstant;
import com.logistics.hub.feature.auth.dto.request.CreateAccountRequest;
import com.logistics.hub.feature.auth.dto.request.UpdateAccountRequest;
import com.logistics.hub.feature.auth.dto.response.UserResponse;
import com.logistics.hub.feature.auth.repository.PasswordResetTokenRepository;
import com.logistics.hub.feature.auth.repository.RefreshTokenRepository;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.auth.service.PasswordResetMailService;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.auth.util.JwtUtils;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.driver.entity.DriverEntity;
import com.logistics.hub.feature.driver.repository.DriverRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private DriverRepository driverRepository;

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

        when(authorizationService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findByIdWithAssignedDepots(2L)).thenReturn(Optional.of(target));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> authService.updateAccount(2L, request));

        assertEquals(AuthConstant.CANNOT_MANAGE_OTHER_ADMIN_ACCOUNT, ex.getMessage());
        verify(userRepository, never()).save(target);
    }

    @Test
    void updateAccount_shouldAllowAdminToUpdateOwnProfile() {
        UserEntity actor = adminUser(1L, "admin01");
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail("updated-admin@example.com");

        when(authorizationService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findByIdWithAssignedDepots(1L)).thenReturn(Optional.of(actor), Optional.of(actor));
        when(userRepository.findByEmailIgnoreCase("updated-admin@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(actor)).thenReturn(actor);
        when(userMapper.toResponse(actor)).thenReturn(new UserResponse());

        authService.updateAccount(1L, request);

        assertEquals("updated-admin@example.com", actor.getEmail());
        verify(userRepository).save(actor);
        verify(depotRepository, never()).findByDispatcher_Id(1L);
    }

    @Test
    void deleteAccount_shouldRejectWhenAdminDeletesAnotherAdmin() {
        UserEntity actor = adminUser(1L, "admin01");
        UserEntity target = adminUser(2L, "admin02");

        when(authorizationService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findByIdWithAssignedDepots(2L)).thenReturn(Optional.of(target));

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> authService.deleteAccount(2L));

        assertEquals(AuthConstant.CANNOT_MANAGE_OTHER_ADMIN_ACCOUNT, ex.getMessage());
        verify(userRepository, never()).delete(target);
    }

    @Test
    void updateAccount_shouldOnlyUpdateProvidedEmailWithoutTouchingDepots() {
        UserEntity actor = adminUser(1L, "admin01");
        UserEntity target = scopedUser(3L, "dispatcher01", "dispatcher01@example.com");

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail("updated@example.com");

        when(authorizationService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findByIdWithAssignedDepots(3L)).thenReturn(Optional.of(target), Optional.of(target));
        when(userRepository.findByEmailIgnoreCase("updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(target)).thenReturn(target);
        when(userMapper.toResponse(target)).thenReturn(new UserResponse());

        authService.updateAccount(3L, request);

        assertEquals("updated@example.com", target.getEmail());
        verify(depotRepository, never()).findByDispatcher_Id(3L);
        verify(depotRepository, never()).findAll();
    }

    @Test
    void updateAccount_shouldLookupCurrentDepotsByDispatcherWhenDepotAssignmentsChange() {
        UserEntity actor = adminUser(1L, "admin01");
        UserEntity target = scopedUser(3L, "dispatcher01", "dispatcher01@example.com");
        DepotEntity depot = new DepotEntity();
        depot.setId(10L);
        depot.setDispatcher(target);

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setAssignedDepotIds(List.of(10L));

        when(authorizationService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findByIdWithAssignedDepots(3L)).thenReturn(Optional.of(target), Optional.of(target));
        when(depotRepository.findByDispatcher_Id(3L)).thenReturn(List.of(depot));
        when(depotRepository.findAllById(anyCollection())).thenReturn(List.of(depot));
        when(userMapper.toResponse(target)).thenReturn(new UserResponse());
        authService.updateAccount(3L, request);

        verify(depotRepository).findByDispatcher_Id(3L);
        verify(depotRepository, never()).findAll();
    }

    @Test
    void updateAccount_shouldAllowAdminRoleWithoutAssignedDepots() {
        UserEntity actor = adminUser(1L, "admin01");
        UserEntity target = scopedUser(3L, "dispatcher01", "dispatcher01@example.com");
        DepotEntity depot = new DepotEntity();
        depot.setId(10L);
        depot.setDispatcher(target);
        target.setAssignedDepots(new java.util.ArrayList<>(List.of(depot)));

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setRole("ADMIN");

        when(authorizationService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findByIdWithAssignedDepots(3L)).thenReturn(Optional.of(target), Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);
        when(depotRepository.findByDispatcher_Id(3L)).thenReturn(List.of(depot));
        when(userMapper.toResponse(target)).thenReturn(new UserResponse());

        authService.updateAccount(3L, request);

        assertEquals("ADMIN", target.getRole());
        assertEquals(null, depot.getDispatcher());
        verify(depotRepository).findByDispatcher_Id(3L);
        verify(depotRepository).saveAll(anyList());
    }

    @Test
    void createAccount_shouldAllowAdminWithoutAssignedDepots() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("admin02");
        request.setFullName("Admin Two");
        request.setEmail("admin02@example.com");
        request.setPassword("password123");
        request.setRole("ADMIN");
        request.setAssignedDepotIds(List.of(10L));

        UserEntity savedUser = new UserEntity();
        savedUser.setId(5L);
        savedUser.setUsername("admin02");
        savedUser.setFullName("Admin Two");
        savedUser.setEmail("admin02@example.com");
        savedUser.setRole("ADMIN");
        savedUser.setAssignedDepots(new java.util.ArrayList<>());

        when(userRepository.existsByUsername("admin02")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("admin02@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(depotRepository.findByDispatcher_Id(5L)).thenReturn(List.of());
        when(userRepository.findByIdWithAssignedDepots(5L)).thenReturn(Optional.of(savedUser));
        when(userMapper.toResponse(savedUser)).thenReturn(new UserResponse());

        authService.createAccount(request);

        verify(depotRepository, never()).findAllById(anyCollection());
        verify(depotRepository, never()).saveAll(anyList());
    }

    @Test
    void createAccount_shouldRejectWhenAssignedDepotAlreadyBelongsToAnotherDispatcher() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("dispatcher02");
        request.setFullName("Dispatcher Two");
        request.setEmail("dispatcher02@example.com");
        request.setPassword("password123");
        request.setRole("DISPATCHER");
        request.setAssignedDepotIds(List.of(10L));

        UserEntity savedUser = scopedUser(6L, "dispatcher02", "dispatcher02@example.com");
        UserEntity existingDispatcher = scopedUser(7L, "dispatcher01", "dispatcher01@example.com");
        DepotEntity depot = new DepotEntity();
        depot.setId(10L);
        depot.setDispatcher(existingDispatcher);

        when(userRepository.existsByUsername("dispatcher02")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("dispatcher02@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(depotRepository.findByDispatcher_Id(6L)).thenReturn(List.of());
        when(depotRepository.findAllById(anyCollection())).thenReturn(List.of(depot));

        ValidationException ex = assertThrows(ValidationException.class, () -> authService.createAccount(request));

        assertEquals("One or more assigned depots already belong to another dispatcher.", ex.getMessage());
        verify(depotRepository, never()).saveAll(anyList());
    }

    @Test
    void createAccount_shouldRejectDriverRoleWithoutDriverId() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("driver01");
        request.setFullName("Driver One");
        request.setEmail("driver01@example.com");
        request.setPassword("password123");
        request.setRole("DRIVER");

        when(userRepository.existsByUsername("driver01")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("driver01@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        ValidationException ex = assertThrows(ValidationException.class, () -> authService.createAccount(request));

        assertEquals("Driver account must be linked to a driver profile.", ex.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void createAccount_shouldAllowDriverRoleWithLinkedDriver() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("driver01");
        request.setFullName("Driver One");
        request.setEmail("driver01@example.com");
        request.setPassword("password123");
        request.setRole("DRIVER");
        request.setDriverId(20L);

        DriverEntity driver = driver(20L, "Driver One");

        when(userRepository.existsByUsername("driver01")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("driver01@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(driverRepository.findById(20L)).thenReturn(Optional.of(driver));
        when(userRepository.existsByDriver_Id(20L)).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(8L);
            return user;
        });
        when(depotRepository.findByDispatcher_Id(8L)).thenReturn(List.of());
        when(userRepository.findByIdWithAssignedDepots(8L)).thenAnswer(invocation -> {
            UserEntity user = new UserEntity();
            user.setId(8L);
            user.setUsername("driver01");
            user.setFullName("Driver One");
            user.setEmail("driver01@example.com");
            user.setRole("DRIVER");
            user.setDriver(driver);
            user.setAssignedDepots(new java.util.ArrayList<>());
            return Optional.of(user);
        });
        when(userMapper.toResponse(any(UserEntity.class))).thenReturn(new UserResponse());

        authService.createAccount(request);

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user -> user.getDriver() != null
                && user.getDriver().getId().equals(20L)));
    }

    @Test
    void createAccount_shouldRejectDriverAlreadyLinkedToAnotherAccount() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("driver02");
        request.setFullName("Driver Two");
        request.setEmail("driver02@example.com");
        request.setPassword("password123");
        request.setRole("DRIVER");
        request.setDriverId(20L);

        when(userRepository.existsByUsername("driver02")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("driver02@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(driverRepository.findById(20L)).thenReturn(Optional.of(driver(20L, "Driver One")));
        when(userRepository.existsByDriver_Id(20L)).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> authService.createAccount(request));

        assertEquals("Driver is already linked to another account.", ex.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void updateAccount_shouldClearDriverWhenRoleChangesAwayFromDriver() {
        UserEntity actor = adminUser(1L, "admin01");
        UserEntity target = scopedUser(3L, "driver01", "driver01@example.com");
        target.setRole("DRIVER");
        target.setDriver(driver(20L, "Driver One"));

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setRole("ADMIN");

        when(authorizationService.getCurrentUser()).thenReturn(actor);
        when(userRepository.findByIdWithAssignedDepots(3L)).thenReturn(Optional.of(target), Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);
        when(depotRepository.findByDispatcher_Id(3L)).thenReturn(List.of());
        when(userMapper.toResponse(target)).thenReturn(new UserResponse());

        authService.updateAccount(3L, request);

        assertEquals("ADMIN", target.getRole());
        assertNull(target.getDriver());
        verify(userRepository).save(target);
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

    private UserEntity scopedUser(Long id, String username, String email) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setUsername(username);
        user.setFullName(username);
        user.setEmail(email);
        user.setRole("DISPATCHER");
        user.setAssignedDepots(new java.util.ArrayList<>());
        return user;
    }

    private DriverEntity driver(Long id, String name) {
        DriverEntity driver = new DriverEntity();
        driver.setId(id);
        driver.setName(name);
        driver.setLicenseNumber("LIC-" + id);
        driver.setPhoneNumber("0900" + id);
        driver.setEmail("driver" + id + "@example.com");
        return driver;
    }
}
