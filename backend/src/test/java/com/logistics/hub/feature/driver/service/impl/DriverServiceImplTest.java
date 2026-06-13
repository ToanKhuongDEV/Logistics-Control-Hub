package com.logistics.hub.feature.driver.service.impl;

import com.logistics.hub.common.exception.ForbiddenException;
import com.logistics.hub.feature.audit.service.AuditActorService;
import com.logistics.hub.feature.audit.service.AuditLogService;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.driver.dto.response.DriverResponse;
import com.logistics.hub.feature.driver.entity.DriverEntity;
import com.logistics.hub.feature.driver.mapper.DriverMapper;
import com.logistics.hub.feature.driver.repository.DriverRepository;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverMapper driverMapper;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private AuditActorService auditActorService;

    @InjectMocks
    private DriverServiceImpl driverService;

    @Test
    void findById_shouldRejectWhenDriverIsOutsideAccessibleDepotScope() {
        DriverEntity driver = driver(15L, "Scoped Driver");

        doNothing().when(authorizationService).requirePermission("driver.read");
        when(driverRepository.findById(15L)).thenReturn(Optional.of(driver));
        doThrow(new ForbiddenException("Ban khong co quyen truy cap tai xe nay."))
                .when(authorizationService).requireDriverAccess(driver);

        assertThrows(ForbiddenException.class, () -> driverService.findById(15L));
        verify(driverMapper, never()).toResponse(driver);
    }

    @Test
    void getAvailableDrivers_shouldReturnUnassignedDriversForScopedUsers() {
        DriverEntity availableDriver = driver(20L, "Available Driver");
        DriverResponse mapped = new DriverResponse();
        mapped.setId(20L);

        doNothing().when(authorizationService).requirePermission("driver.read");
        when(authorizationService.hasGlobalScope()).thenReturn(false);
        when(driverRepository.findAvailableDrivers(null)).thenReturn(List.of(availableDriver));
        when(driverMapper.toResponse(availableDriver)).thenReturn(mapped);

        List<DriverResponse> response = driverService.getAvailableDrivers(null);

        assertEquals(List.of(mapped), response);
        verify(driverRepository).findAvailableDrivers(null);
    }

    @Test
    void getAvailableDrivers_shouldReturnUnassignedAndIncludedDriverWhenScopedAccessIsAllowed() {
        DriverEntity currentDriver = driver(21L, "Depot Driver");
        DriverEntity availableDriver = driver(22L, "Available Driver");
        DriverResponse currentMapped = new DriverResponse();
        currentMapped.setId(21L);
        DriverResponse availableMapped = new DriverResponse();
        availableMapped.setId(22L);

        doNothing().when(authorizationService).requirePermission("driver.read");
        when(authorizationService.hasGlobalScope()).thenReturn(false);
        when(driverRepository.findById(21L)).thenReturn(Optional.of(currentDriver));
        doNothing().when(authorizationService).requireDriverAccess(currentDriver);
        when(driverRepository.findAvailableDrivers(21L)).thenReturn(List.of(currentDriver, availableDriver));
        when(driverMapper.toResponse(currentDriver)).thenReturn(currentMapped);
        when(driverMapper.toResponse(availableDriver)).thenReturn(availableMapped);

        List<DriverResponse> response = driverService.getAvailableDrivers(21L);

        assertEquals(2, response.size());
        assertEquals(21L, response.get(0).getId());
        assertEquals(22L, response.get(1).getId());
        verify(authorizationService).requireDriverAccess(currentDriver);
        verify(driverRepository).findAvailableDrivers(21L);
    }

    @Test
    void getAvailableDrivers_shouldNotCacheAssignmentDependentResults() throws NoSuchMethodException {
        Method method = DriverServiceImpl.class.getMethod("getAvailableDrivers", Long.class);

        assertFalse(method.isAnnotationPresent(Cacheable.class));
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
