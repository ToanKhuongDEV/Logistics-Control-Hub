package com.logistics.hub.feature.driver.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
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

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional(readOnly = true)
    public Page<DriverResponse> findAll(Pageable pageable, String search, Long depotId) {
        if (authorizationService.isAdmin()) {
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
        DriverEntity driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DriverConstant.DRIVER_NOT_FOUND));
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
        authorizationService.requireAdmin();
        normalizeRequest(request);
        validateDriverRequest(request, null);
        DriverEntity driver = driverMapper.toEntity(request);
        DriverEntity savedDriver = driverRepository.save(driver);
        return driverMapper.toResponse(savedDriver);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstant.DRIVERS, allEntries = true),
            @CacheEvict(value = CacheConstant.DRIVER_STATS, allEntries = true),
            @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
    })
    public DriverResponse update(Long id, DriverRequest request) {
        authorizationService.requireAdmin();
        DriverEntity driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(DriverConstant.DRIVER_NOT_FOUND));

        normalizeRequest(request);
        validateDriverRequest(request, id);

        driverMapper.updateEntityFromRequest(request, driver);
        DriverEntity updatedDriver = driverRepository.save(driver);
        return driverMapper.toResponse(updatedDriver);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstant.DRIVERS, allEntries = true),
            @CacheEvict(value = CacheConstant.DRIVER_STATS, allEntries = true),
            @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
    })
    public void delete(Long id) {
        authorizationService.requireAdmin();
        if (!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException(DriverConstant.DRIVER_NOT_FOUND);
        }

        if (vehicleRepository.existsByDriver_Id(id)) {
            throw new ValidationException(DriverConstant.DRIVER_HAS_VEHICLE);
        }

        if (orderRepository.existsByDriver_Id(id)) {
            throw new ValidationException(DriverConstant.DRIVER_HAS_ORDERS);
        }

        try {
            driverRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(DriverConstant.DRIVER_HAS_VEHICLE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstant.DRIVERS, key = "'available:' + #includeDriverId")
    public List<DriverResponse> getAvailableDrivers(Long includeDriverId) {
        return driverRepository.findAvailableDrivers(includeDriverId)
                .stream()
                .map(driverMapper::toResponse)
                .toList();
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

        if (authorizationService.isAdmin()) {
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

}
