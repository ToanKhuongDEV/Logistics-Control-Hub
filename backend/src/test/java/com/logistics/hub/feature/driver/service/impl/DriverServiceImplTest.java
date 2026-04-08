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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void getAvailableDrivers_shouldReturnEmptyForScopedUsersWithoutIncludedDriver() {
        doNothing().when(authorizationService).requirePermission("driver.read");
        when(authorizationService.hasGlobalScope()).thenReturn(false);

        List<DriverResponse> response = driverService.getAvailableDrivers(null);

        assertTrue(response.isEmpty());
        verify(driverRepository, never()).findAvailableDrivers(null);
        verify(driverRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void getAvailableDrivers_shouldOnlyReturnIncludedDriverWhenScopedAccessIsAllowed() {
        DriverEntity driver = driver(21L, "Depot Driver");
        DriverResponse mapped = new DriverResponse();
        mapped.setId(21L);
        mapped.setName("Depot Driver");

        doNothing().when(authorizationService).requirePermission("driver.read");
        when(authorizationService.hasGlobalScope()).thenReturn(false);
        when(driverRepository.findById(21L)).thenReturn(Optional.of(driver));
        doNothing().when(authorizationService).requireDriverAccess(driver);
        when(driverMapper.toResponse(driver)).thenReturn(mapped);

        List<DriverResponse> response = driverService.getAvailableDrivers(21L);

        assertEquals(1, response.size());
        assertEquals(21L, response.get(0).getId());
        verify(driverRepository, never()).findAvailableDrivers(21L);
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
