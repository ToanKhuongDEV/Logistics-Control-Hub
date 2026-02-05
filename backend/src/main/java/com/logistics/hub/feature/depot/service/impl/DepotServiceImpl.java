package com.logistics.hub.feature.depot.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.depot.constant.DepotConstant;
import com.logistics.hub.feature.depot.dto.request.DepotRequest;
import com.logistics.hub.feature.depot.dto.response.DepotResponse;
import com.logistics.hub.feature.depot.dto.response.DepotStatisticsResponse;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.depot.mapper.DepotMapper;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.depot.service.DepotService;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.location.repository.LocationRepository;
import com.logistics.hub.feature.location.service.LocationService;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepotServiceImpl implements DepotService {

  private final DepotRepository depotRepository;
  private final DepotMapper depotMapper;
  private final LocationRepository locationRepository;
  private final LocationService locationService;
  private final VehicleRepository vehicleRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<DepotResponse> findAll(String search, Pageable pageable) {
    Sort sort = Sort.by(
        Sort.Order.desc("isActive"),
        Sort.Order.asc("id"));
    Pageable sortedPageable = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sort);

    Page<DepotEntity> depotPage;
    if (search != null && !search.trim().isEmpty()) {
      depotPage = depotRepository.searchDepots(search, sortedPageable);
    } else {
      depotPage = depotRepository.findAll(sortedPageable);
    }
    return depotPage.map(this::enrichResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public DepotResponse findById(Long id) {
    DepotEntity entity = depotRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(DepotConstant.DEPOT_NOT_FOUND + id));
    return enrichResponse(entity);
  }

  @Override
  public DepotResponse create(DepotRequest request) {
    LocationEntity location = locationService.getOrCreateLocation(request.getLocationRequest());
    Long locationId = location.getId();

    if (depotRepository.existsByLocationId(locationId)) {
      throw new ValidationException(DepotConstant.DEPOT_LOCATION_EXISTS);
    }

    DepotEntity entity = depotMapper.toEntity(request);
    entity.setLocationId(locationId);

    DepotEntity saved = depotRepository.save(entity);
    return enrichResponse(saved);
  }

  @Override
  public DepotResponse update(Long id, DepotRequest request) {
    DepotEntity entity = depotRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(DepotConstant.DEPOT_NOT_FOUND + id));

    LocationEntity location = locationService.getOrCreateLocation(request.getLocationRequest());
    Long newLocationId = location.getId();

    if (depotRepository.existsByLocationIdAndIdNot(newLocationId, id)) {
      throw new ValidationException(DepotConstant.DEPOT_LOCATION_EXISTS);
    }

    depotMapper.updateEntityFromRequest(request, entity);
    entity.setLocationId(newLocationId);

    DepotEntity saved = depotRepository.save(entity);
    return enrichResponse(saved);
  }

  @Override
  public void delete(Long id) {
    if (!depotRepository.existsById(id)) {
      throw new ResourceNotFoundException(DepotConstant.DEPOT_NOT_FOUND + id);
    }

    if (vehicleRepository.existsByDepotId(id)) {
      throw new ValidationException(DepotConstant.DEPOT_HAS_VEHICLES);
    }

    try {
      depotRepository.deleteById(id);
    } catch (DataIntegrityViolationException e) {
      throw new ValidationException(DepotConstant.DEPOT_HAS_VEHICLES);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public DepotStatisticsResponse getStatistics() {
    long total = depotRepository.count();
    long active = depotRepository.countByIsActive(true);
    long inactive = depotRepository.countByIsActive(false);

    return new DepotStatisticsResponse(total, active, inactive);
  }

  private DepotResponse enrichResponse(DepotEntity entity) {
    DepotResponse response = depotMapper.toResponse(entity);
    if (entity.getLocationId() != null) {
      locationRepository.findById(entity.getLocationId())
          .ifPresent(location -> {
            String fullAddress = String.format("%s, %s, %s",
                location.getStreet(),
                location.getCity(),
                location.getCountry());
            response.setAddress(fullAddress);
          });
    }
    return response;
  }
}
