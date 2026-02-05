package com.logistics.hub.feature.vehicle.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.driver.repository.DriverRepository;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.location.repository.LocationRepository;
import com.logistics.hub.feature.vehicle.constant.VehicleConstant;
import com.logistics.hub.feature.vehicle.dto.request.VehicleRequest;
import com.logistics.hub.feature.vehicle.dto.response.VehicleResponse;
import com.logistics.hub.feature.vehicle.dto.response.VehicleStatisticsResponse;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import com.logistics.hub.feature.vehicle.mapper.VehicleMapper;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import com.logistics.hub.feature.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final DriverRepository driverRepository;
    private final DepotRepository depotRepository;
    private final LocationRepository locationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findAll(Pageable pageable, VehicleStatus status, String search) {
        Page<VehicleEntity> vehiclePage = vehicleRepository.findByStatusAndSearch(status, search, pageable);
        return vehiclePage.map(this::enrichResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) {
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(VehicleConstant.VEHICLE_NOT_FOUND + id));
        return enrichResponse(entity);
    }

    @Override
    public VehicleResponse create(VehicleRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            request.setCode(generateVehicleCode(request.getMaxWeightKg()));
        } else if (vehicleRepository.existsByCode(request.getCode())) {
            throw new ValidationException(VehicleConstant.VEHICLE_CODE_EXISTS + request.getCode());
        }

        if (request.getDriverId() != null && vehicleRepository.existsByDriverId(request.getDriverId())) {
            throw new ValidationException(VehicleConstant.DRIVER_ALREADY_ASSIGNED);
        }

        VehicleEntity entity = vehicleMapper.toEntity(request);
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
    public VehicleResponse update(Long id, VehicleRequest request) {
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(VehicleConstant.VEHICLE_NOT_FOUND + id));

        if (!entity.getCode().equals(request.getCode()) && vehicleRepository.existsByCode(request.getCode())) {
            throw new ValidationException(VehicleConstant.VEHICLE_CODE_EXISTS + request.getCode());
        }

        if (request.getDriverId() != null && vehicleRepository.existsByDriverIdAndIdNot(request.getDriverId(), id)) {
            throw new ValidationException(VehicleConstant.DRIVER_ALREADY_ASSIGNED);
        }

        vehicleMapper.updateEntityFromRequest(request, entity);
        VehicleEntity saved = vehicleRepository.save(entity);
        return enrichResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFoundException(VehicleConstant.VEHICLE_NOT_FOUND + id);
        }
        vehicleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleStatisticsResponse getStatistics() {
        List<VehicleEntity> allVehicles = vehicleRepository.findAll();

        long total = allVehicles.size();
        long active = vehicleRepository.countByStatus(VehicleStatus.ACTIVE);
        long maintenance = vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE);
        long idle = vehicleRepository.countByStatus(VehicleStatus.IDLE);

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
        VehicleResponse response = vehicleMapper.toResponse(entity);
        if (entity.getDriverId() != null) {
            driverRepository.findById(entity.getDriverId())
                    .ifPresent(driver -> response.setDriverName(driver.getName()));
        }
        if (entity.getDepotId() != null) {
            depotRepository.findById(entity.getDepotId())
                    .ifPresent(depot -> {
                        response.setDepotName(depot.getName());
                        response.setLocationId(depot.getLocationId());
                        if (depot.getLocationId() != null) {
                            locationRepository.findById(depot.getLocationId())
                                    .ifPresent(location -> {
                                        String fullAddress = String.format("%s, %s, %s",
                                                location.getStreet(),
                                                location.getCity(),
                                                location.getCountry());
                                        response.setAddress(fullAddress);
                                    });
                        }
                    });
        }
        return response;
    }
}
