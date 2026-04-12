package com.logistics.hub.feature.vehicle.service.impl;

import com.logistics.hub.common.exception.ForbiddenException;
import com.logistics.hub.feature.audit.service.AuditActorService;
import com.logistics.hub.feature.audit.service.AuditLogService;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.driver.entity.DriverEntity;
import com.logistics.hub.feature.driver.repository.DriverRepository;
import com.logistics.hub.feature.vehicle.dto.request.VehicleRequest;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import com.logistics.hub.feature.vehicle.mapper.VehicleMapper;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VehicleServiceImplTest {

    @Test
    void create_shouldRejectAssigningDriverFromAnotherDepotToScopedDispatcher() {
        VehicleRepository vehicleRepository = mock(VehicleRepository.class);
        VehicleMapper vehicleMapper = mock(VehicleMapper.class);
        DepotRepository depotRepository = mock(DepotRepository.class);
        DriverRepository driverRepository = mock(DriverRepository.class);
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        AuditLogService auditLogService = mock(AuditLogService.class);
        AuditActorService auditActorService = mock(AuditActorService.class);
        VehicleServiceImpl vehicleService = new VehicleServiceImpl(
                vehicleRepository,
                vehicleMapper,
                depotRepository,
                driverRepository,
                authorizationService,
                auditLogService,
                auditActorService);

        VehicleRequest request = new VehicleRequest();
        request.setCode("TRK-001");
        request.setMaxWeightKg(1000);
        request.setMaxVolumeM3(BigDecimal.valueOf(10));
        request.setCostPerKm(BigDecimal.valueOf(5));
        request.setStatus(VehicleStatus.ACTIVE);
        request.setType("TRUCK");
        request.setDepotId(1L);
        request.setDriverId(99L);

        DriverEntity driver = new DriverEntity();
        driver.setId(99L);
        doNothing().when(authorizationService).requirePermission("vehicle.manage");
        doNothing().when(authorizationService).requireDepotAccess(1L);
        when(vehicleRepository.existsByCode("TRK-001")).thenReturn(false);
        when(driverRepository.findById(99L)).thenReturn(java.util.Optional.of(driver));
        when(authorizationService.hasGlobalScope()).thenReturn(false);
        when(authorizationService.getAccessibleDepotIds()).thenReturn(Set.of(1L));
        when(vehicleRepository.existsByDriver_Id(99L)).thenReturn(true);
        when(vehicleRepository.existsByDriver_IdAndDepot_IdIn(99L, Set.of(1L))).thenReturn(false);
        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> vehicleService.create(request));

        assertEquals("Khong the gan tai xe tu kho khac.", ex.getMessage());
        verify(vehicleRepository, never()).save(org.mockito.ArgumentMatchers.any(VehicleEntity.class));
    }
}
