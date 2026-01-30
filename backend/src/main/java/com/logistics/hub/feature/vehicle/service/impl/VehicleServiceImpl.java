package com.logistics.hub.feature.vehicle.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
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

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> findAll(Pageable pageable, VehicleStatus status, String search) {
        Page<VehicleEntity> vehiclePage = vehicleRepository.findByStatusAndSearch(status, search, pageable);
        return vehiclePage.map(vehicleMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) {
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(VehicleConstant.VEHICLE_NOT_FOUND + id));
        return vehicleMapper.toResponse(entity);
    }

    @Override
    public VehicleResponse create(VehicleRequest request) {
        if (vehicleRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException(VehicleConstant.VEHICLE_CODE_EXISTS + request.getCode());
        }
        
        VehicleEntity entity = vehicleMapper.toEntity(request);
        VehicleEntity saved = vehicleRepository.save(entity);
        return vehicleMapper.toResponse(saved);
    }

    @Override
    public VehicleResponse update(Long id, VehicleRequest request) {
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(VehicleConstant.VEHICLE_NOT_FOUND + id));
        
        if (!entity.getCode().equals(request.getCode()) && vehicleRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException(VehicleConstant.VEHICLE_CODE_EXISTS + request.getCode());
        }
        
        vehicleMapper.updateEntityFromRequest(request, entity);
        VehicleEntity saved = vehicleRepository.save(entity);
        return vehicleMapper.toResponse(saved);
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
                totalCapacityM3
        );
    }
}
