package com.logistics.hub.feature.vehicle.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.common.exception.ForbiddenException;
import com.logistics.hub.feature.auth.policy.AuthorizationPolicy;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.driver.entity.DriverEntity;
import com.logistics.hub.feature.driver.repository.DriverRepository;
import com.logistics.hub.feature.vehicle.constant.VehicleConstant;
import com.logistics.hub.feature.vehicle.dto.request.VehicleRequest;
import com.logistics.hub.feature.vehicle.dto.response.VehicleResponse;
import com.logistics.hub.feature.vehicle.dto.response.VehicleStatisticsResponse;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import com.logistics.hub.feature.vehicle.mapper.VehicleMapper;
import com.logistics.hub.feature.redis.constant.CacheConstant;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import com.logistics.hub.feature.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final DepotRepository depotRepository;
    private final DriverRepository driverRepository;
    private final AuthorizationService authorizationService;

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findAll(Pageable pageable, VehicleStatus status, String search, Long depotId) {
        Page<VehicleEntity> vehiclePage;
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_VEHICLE_READ);
        if (authorizationService.hasGlobalScope()) {
            vehiclePage = vehicleRepository.findByStatusAndSearchAndDepot(status, search, depotId, pageable);
        } else if (depotId != null) {
            authorizationService.requireDepotAccess(depotId);
            vehiclePage = vehicleRepository.findByStatusAndSearchAndDepot(status, search, depotId, pageable);
        } else {
            if (authorizationService.getAccessibleDepotIds().isEmpty()) {
                return Page.empty(pageable);
            }
            vehiclePage = vehicleRepository.findByStatusAndSearchAndDepotIds(
                    status,
                    search,
                    authorizationService.getAccessibleDepotIds(),
                    pageable);
        }
        return vehiclePage.map(this::enrichResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstant.VEHICLES, key = "'id:' + #id")
    public VehicleResponse findById(Long id) {
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(VehicleConstant.VEHICLE_NOT_FOUND + id));
        authorizationService.requireVehicleAccess(entity);
        return enrichResponse(entity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConstant.VEHICLES, allEntries = true),
            @CacheEvict(value = CacheConstant.VEHICLE_STATS, allEntries = true),
            @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
    })
    public VehicleResponse create(VehicleRequest request) {
        if (request.getDepotId() != null) {
            authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_VEHICLE_MANAGE);
            authorizationService.requireDepotAccess(request.getDepotId());
        } else {
            authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_VEHICLE_REASSIGN);
        }

        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            request.setCode(generateVehicleCode(request.getMaxWeightKg()));
        } else if (vehicleRepository.existsByCode(request.getCode())) {
            throw new ValidationException(VehicleConstant.VEHICLE_CODE_EXISTS + request.getCode());
        }

        if (request.getDriverId() != null && vehicleRepository.existsByDriver_Id(request.getDriverId())) {
            throw new ValidationException(VehicleConstant.DRIVER_ALREADY_ASSIGNED);
        }

        VehicleEntity entity = vehicleMapper.toEntity(request);
        resolveAndSetDepotDriver(request, entity);
        VehicleEntity saved = vehicleRepository.save(entity);
        return enrichResponse(saved);
    }

    private String generateVehicleCode(Integer weightKg) {
        String prefix;
        if (weightKg < 3500) {
            prefix = "LDT";
        } else if (weightKg < 8000) {
            prefix = "MDT";
        } else {
            prefix = "HDT";
        }

        String latestCode = vehicleRepository.findLatestCodeByPrefix(prefix);
        int nextNumber = 1;

        if (latestCode != null && !latestCode.isEmpty()) {
            try {
                String numberPart = latestCode.substring(prefix.length() + 1);
                nextNumber = Integer.parseInt(numberPart) + 1;
            } catch (NumberFormatException e) {
                nextNumber = 1;
            }
        }

        return String.format("%s-%03d", prefix, nextNumber);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConstant.VEHICLES, allEntries = true),
            @CacheEvict(value = CacheConstant.VEHICLE_STATS, allEntries = true),
            @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
    })
    public VehicleResponse update(Long id, VehicleRequest request) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_VEHICLE_MANAGE);
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(VehicleConstant.VEHICLE_NOT_FOUND + id));
        authorizationService.requireVehicleAccess(entity);

        if (!authorizationService.hasPermission(AuthorizationPolicy.PERMISSION_VEHICLE_REASSIGN)
                && request.getDepotId() != null
                && entity.getDepot() != null
                && !entity.getDepot().getId().equals(request.getDepotId())) {
            throw new ForbiddenException("Điều chuyển phương tiện sang kho khác cần admin xử lý.");
        }

        if (!entity.getCode().equals(request.getCode()) && vehicleRepository.existsByCode(request.getCode())) {
            throw new ValidationException(VehicleConstant.VEHICLE_CODE_EXISTS + request.getCode());
        }

        if (request.getDriverId() != null && vehicleRepository.existsByDriver_IdAndIdNot(request.getDriverId(), id)) {
            throw new ValidationException(VehicleConstant.DRIVER_ALREADY_ASSIGNED);
        }

        vehicleMapper.updateEntityFromRequest(request, entity);
        resolveAndSetDepotDriver(request, entity);
        VehicleEntity saved = vehicleRepository.save(entity);
        return enrichResponse(saved);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConstant.VEHICLES, allEntries = true),
            @CacheEvict(value = CacheConstant.VEHICLE_STATS, allEntries = true),
            @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
    })
    public void updateDepotBulk(Long depotId, List<Long> vehicleIds) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_VEHICLE_REASSIGN);
        DepotEntity depot = depotRepository.findById(depotId)
                .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + depotId));

        List<VehicleEntity> vehicles = vehicleRepository.findAllById(vehicleIds);

        if (vehicles.size() != vehicleIds.size()) {
            Set<Long> foundIds = vehicles.stream()
                    .map(VehicleEntity::getId)
                    .collect(Collectors.toSet());

            String missingIds = vehicleIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            throw new ValidationException(VehicleConstant.VEHICLE_IDS_NOT_FOUND + missingIds);
        }

        vehicles.forEach(authorizationService::requireVehicleAccess);
        vehicles.forEach(vehicle -> vehicle.setDepot(depot));
        vehicleRepository.saveAll(vehicles);
    }

    private void resolveAndSetDepotDriver(VehicleRequest request, VehicleEntity entity) {
        if (request.getDepotId() != null) {
            authorizationService.requireDepotAccess(request.getDepotId());
            DepotEntity depot = depotRepository.findById(request.getDepotId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Depot not found with id: " + request.getDepotId()));
            entity.setDepot(depot);
        } else {
            entity.setDepot(null);
        }

        if (request.getDriverId() != null) {
            DriverEntity driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Driver not found with id: " + request.getDriverId()));
            entity.setDriver(driver);
        } else {
            entity.setDriver(null);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConstant.VEHICLES, allEntries = true),
            @CacheEvict(value = CacheConstant.VEHICLE_STATS, allEntries = true),
            @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
    })
    public void delete(Long id) {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_VEHICLE_MANAGE);
        VehicleEntity vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(VehicleConstant.VEHICLE_NOT_FOUND + id));
        authorizationService.requireVehicleAccess(vehicle);
        vehicleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleStatisticsResponse getStatistics() {
        authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_VEHICLE_READ);
        List<VehicleEntity> allVehicles = authorizationService.hasGlobalScope()
                ? vehicleRepository.findAll()
                : vehicleRepository.findByStatusAndSearchAndDepotIds(
                        null,
                        null,
                        authorizationService.getAccessibleDepotIds(),
                        Pageable.unpaged()).getContent();

        long total = allVehicles.size();
        long active = allVehicles.stream().filter(vehicle -> vehicle.getStatus() == VehicleStatus.ACTIVE).count();
        long maintenance = allVehicles.stream().filter(vehicle -> vehicle.getStatus() == VehicleStatus.MAINTENANCE).count();
        long idle = allVehicles.stream().filter(vehicle -> vehicle.getStatus() == VehicleStatus.IDLE).count();

        BigDecimal averageCostPerKm = allVehicles.stream()
                .map(VehicleEntity::getCostPerKm)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(total > 0 ? total : 1), 2, RoundingMode.HALF_UP);

        Long totalCapacityKg = allVehicles.stream()
                .map(VehicleEntity::getMaxWeightKg)
                .filter(weight -> weight != null)
                .mapToLong(Integer::longValue)
                .sum();

        BigDecimal totalCapacityM3 = allVehicles.stream()
                .map(VehicleEntity::getMaxVolumeM3)
                .filter(volume -> volume != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new VehicleStatisticsResponse(
                total,
                active,
                maintenance,
                idle,
                averageCostPerKm,
                totalCapacityKg,
                totalCapacityM3);
    }

    private VehicleResponse enrichResponse(VehicleEntity entity) {
        return vehicleMapper.toResponse(entity);
    }
}
