package com.logistics.hub.feature.driver.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.common.exception.ForbiddenException;
import com.logistics.hub.feature.audit.constant.AuditAction;
import com.logistics.hub.feature.audit.constant.AuditResourceType;
import com.logistics.hub.feature.audit.constant.AuditStatus;
import com.logistics.hub.feature.audit.service.AuditActorService;
import com.logistics.hub.feature.audit.service.AuditLogService;
import com.logistics.hub.feature.auth.policy.AuthorizationPolicy;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.driver.constant.DriverConstant;
import com.logistics.hub.feature.driver.dto.request.DriverRequest;
import com.logistics.hub.feature.driver.dto.response.DriverResponse;
import com.logistics.hub.feature.driver.entity.DriverEntity;

import com.logistics.hub.feature.driver.mapper.DriverMapper;
import com.logistics.hub.feature.driver.repository.DriverRepository;
import com.logistics.hub.feature.driver.service.DriverService;
import com.logistics.hub.feature.order.repository.OrderRepository;
import com.logistics.hub.feature.redis.constant.CacheConstant;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final AuthorizationService authorizationService;
    private final AuditLogService auditLogService;
    private final AuditActorService auditActorService;

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponse> findAll(Pageable pageable, String search, Long depotId) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_READ);
        if (authorizationService.hasGlobalScope()) {
            return driverRepository.findBySearchAndDepot(search, depotId, pageable)
                    .map(driverMapper::toResponse);
        }

        if (depotId != null) {
            authorizationService.requireDepotAccess(depotId);
            return driverRepository.findBySearchAndDepot(search, depotId, pageable)
                    .map(driverMapper::toResponse);
        }

        Set<Long> accessibleDepotIds = authorizationService.getAccessibleDepotIds();
        if (accessibleDepotIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return driverRepository.findBySearchAndDepotIds(search, accessibleDepotIds, pageable)
                .map(driverMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstant.DRIVERS, key = "'id:' + #id")
    public DriverResponse findById(Long id) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_READ);
        DriverEntity driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DriverConstant.DRIVER_NOT_FOUND));
        authorizationService.requireDriverAccess(driver);
        return driverMapper.toResponse(driver);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstant.DRIVERS, allEntries = true),
            @CacheEvict(value = CacheConstant.DRIVER_STATS, allEntries = true),
            @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
    })
    public DriverResponse create(DriverRequest request) {
        try {
            authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_MANAGE);
            normalizeRequest(request);
            validateDriverRequest(request, null);
            DriverEntity driver = driverMapper.toEntity(request);
            DriverEntity savedDriver = driverRepository.save(driver);
            auditLogService.log(
                    auditActorService.getCurrentActor(),
                    AuditAction.CREATE,
                    AuditResourceType.DRIVER,
                    savedDriver.getId().toString(),
                    savedDriver.getName(),
                    null,
                    AuditStatus.SUCCESS,
                    "Created driver",
                    null,
                    driverAuditSnapshot(savedDriver),
                    null);
            return driverMapper.toResponse(savedDriver);
        } catch (RuntimeException ex) {
            logFailure(AuditAction.CREATE, null, request.getName(), null, driverRequestSnapshot(request), ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstant.DRIVERS, allEntries = true),
            @CacheEvict(value = CacheConstant.DRIVER_STATS, allEntries = true),
            @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
    })
    public DriverResponse update(Long id, DriverRequest request) {
        DriverEntity driver = null;
        Map<String, Object> beforeData = null;
        try {
            authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_MANAGE);
            driver = driverRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(DriverConstant.DRIVER_NOT_FOUND));
            authorizationService.requireDriverAccess(driver);
            beforeData = driverAuditSnapshot(driver);

            normalizeRequest(request);
            validateDriverRequest(request, id);

            driverMapper.updateEntityFromRequest(request, driver);
            DriverEntity updatedDriver = driverRepository.save(driver);
            auditLogService.log(
                    auditActorService.getCurrentActor(),
                    AuditAction.UPDATE,
                    AuditResourceType.DRIVER,
                    updatedDriver.getId().toString(),
                    updatedDriver.getName(),
                    null,
                    AuditStatus.SUCCESS,
                    "Updated driver",
                    beforeData,
                    driverAuditSnapshot(updatedDriver),
                    null);
            return driverMapper.toResponse(updatedDriver);
        } catch (RuntimeException ex) {
            logFailure(AuditAction.UPDATE, id.toString(), driver != null ? driver.getName() : request.getName(), beforeData, driverRequestSnapshot(request), ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstant.DRIVERS, allEntries = true),
            @CacheEvict(value = CacheConstant.DRIVER_STATS, allEntries = true),
            @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
    })
    public void delete(Long id) {
        DriverEntity driver = null;
        try {
            authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_MANAGE);
            driver = driverRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(DriverConstant.DRIVER_NOT_FOUND));
            authorizationService.requireDriverAccess(driver);

            if (vehicleRepository.existsByDriver_Id(id)) {
                throw new ValidationException(DriverConstant.DRIVER_HAS_VEHICLE);
            }

            if (orderRepository.existsByDriver_Id(id)) {
                throw new ValidationException(DriverConstant.DRIVER_HAS_ORDERS);
            }

            try {
                driverRepository.deleteById(id);
                auditLogService.log(
                        auditActorService.getCurrentActor(),
                        AuditAction.DELETE,
                        AuditResourceType.DRIVER,
                        driver.getId().toString(),
                        driver.getName(),
                        null,
                        AuditStatus.SUCCESS,
                        "Deleted driver",
                        driverAuditSnapshot(driver),
                        null,
                        null);
            } catch (DataIntegrityViolationException e) {
                throw new ValidationException(DriverConstant.DRIVER_HAS_VEHICLE);
            }
        } catch (RuntimeException ex) {
            logFailure(AuditAction.DELETE, id.toString(), driver != null ? driver.getName() : null, driver != null ? driverAuditSnapshot(driver) : null, null, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstant.DRIVERS, key = "'available:' + #includeDriverId")
    public List<DriverResponse> getAvailableDrivers(Long includeDriverId) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_READ);
        if (authorizationService.hasGlobalScope()) {
            return driverRepository.findAvailableDrivers(includeDriverId)
                    .stream()
                    .map(driverMapper::toResponse)
                    .toList();
        }

        if (includeDriverId == null) {
            return List.of();
        }

        DriverEntity driver = driverRepository.findById(includeDriverId)
                .orElseThrow(() -> new ResourceNotFoundException(DriverConstant.DRIVER_NOT_FOUND));
        authorizationService.requireDriverAccess(driver);
        return List.of(driverMapper.toResponse(driver));
    }

    private void validateDriverRequest(DriverRequest request, Long id) {
        boolean licenseExists = (id == null) ? driverRepository.existsByLicenseNumber(request.getLicenseNumber())
                : driverRepository.existsByLicenseNumberAndIdNot(request.getLicenseNumber(), id);

        if (licenseExists) {
            throw new ValidationException(DriverConstant.LICENSE_NUMBER_EXISTS);
        }

        boolean phoneExists = (id == null) ? driverRepository.existsByPhoneNumber(request.getPhoneNumber())
                : driverRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), id);

        if (phoneExists) {
            throw new ValidationException(DriverConstant.PHONE_NUMBER_EXISTS);
        }
    }

    private void normalizeRequest(DriverRequest request) {
        if (request.getLicenseNumber() != null) {
            request.setLicenseNumber(request.getLicenseNumber().trim().toUpperCase());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public com.logistics.hub.feature.driver.dto.response.DriverStatisticsResponse getStatistics() {
        long total;
        long available;
        long assigned;

        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DRIVER_READ);
        if (authorizationService.hasGlobalScope()) {
            total = driverRepository.count();
            List<DriverEntity> availableDrivers = driverRepository.findAvailableDrivers(null);
            available = availableDrivers.size();
            assigned = total - available;
        } else {
            total = driverRepository.countDistinctByDepotIds(authorizationService.getAccessibleDepotIds());
            available = 0;
            assigned = total;
        }

        return new com.logistics.hub.feature.driver.dto.response.DriverStatisticsResponse(
                total,
                available,
                assigned);
    }

    private Map<String, Object> driverAuditSnapshot(DriverEntity driver) {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", driver.getId());
        snapshot.put("name", driver.getName());
        snapshot.put("licenseNumber", driver.getLicenseNumber());
        snapshot.put("phoneNumber", driver.getPhoneNumber());
        snapshot.put("email", driver.getEmail());
        return snapshot;
    }

    private Map<String, Object> driverRequestSnapshot(DriverRequest request) {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("name", request.getName());
        snapshot.put("licenseNumber", request.getLicenseNumber());
        snapshot.put("phoneNumber", request.getPhoneNumber());
        snapshot.put("email", request.getEmail());
        return snapshot;
    }

    private void logFailure(String action, String resourceId, String resourceName, Object beforeData, Object afterData, RuntimeException ex) {
        if (!(ex instanceof ValidationException || ex instanceof ForbiddenException || ex instanceof ResourceNotFoundException)) {
            return;
        }

        auditLogService.log(
                auditActorService.getCurrentActor(),
                action,
                AuditResourceType.DRIVER,
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
