package com.logistics.hub.feature.depot.service.impl;

import com.logistics.hub.common.exception.ResourceNotFoundException;
import com.logistics.hub.common.exception.ValidationException;
import com.logistics.hub.feature.auth.service.AuthorizationService;
import com.logistics.hub.feature.depot.constant.DepotConstant;
import com.logistics.hub.feature.depot.dto.request.DepotRequest;
import com.logistics.hub.feature.depot.dto.response.DepotResponse;
import com.logistics.hub.feature.depot.dto.response.DepotStatisticsResponse;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import com.logistics.hub.feature.depot.mapper.DepotMapper;
import com.logistics.hub.feature.depot.repository.DepotRepository;
import com.logistics.hub.feature.depot.service.DepotService;
import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.location.service.LocationService;
import com.logistics.hub.feature.redis.constant.CacheConstant;
import com.logistics.hub.feature.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class DepotServiceImpl implements DepotService {

  private final DepotRepository depotRepository;
  private final DepotMapper depotMapper;
  private final LocationService locationService;
  private final VehicleRepository vehicleRepository;
  private final AuthorizationService authorizationService;

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
    if (authorizationService.isAdmin()) {
      depotPage = (search != null && !search.trim().isEmpty())
          ? depotRepository.searchDepots(search, sortedPageable)
          : depotRepository.findAll(sortedPageable);
    } else {
      Set<Long> accessibleDepotIds = authorizationService.getAccessibleDepotIds();
      if (accessibleDepotIds.isEmpty()) {
        return Page.empty(sortedPageable);
      }
      depotPage = (search != null && !search.trim().isEmpty())
          ? depotRepository.searchDepotsByIds(search, accessibleDepotIds, sortedPageable)
          : depotRepository.findByIdIn(accessibleDepotIds, sortedPageable);
    }
    return depotPage.map(this::enrichResponse);
  }

  @Override
  @Transactional(readOnly = true)
  @Cacheable(value = CacheConstant.DEPOTS, key = "'id:' + #id")
  public DepotResponse findById(Long id) {
    authorizationService.requireDepotAccess(id);
    DepotEntity entity = depotRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(DepotConstant.DEPOT_NOT_FOUND + id));
    return enrichResponse(entity);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheConstant.DEPOTS, allEntries = true),
      @CacheEvict(value = CacheConstant.DEPOT_STATS, allEntries = true),
      @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
  })
  public DepotResponse create(DepotRequest request) {
    authorizationService.requireAdmin();
    LocationEntity location = locationService.getOrCreateLocation(request.getLocationRequest());
    Long locationId = location.getId();

    if (depotRepository.existsByLocation_Id(locationId)) {
      throw new ValidationException(DepotConstant.DEPOT_LOCATION_EXISTS);
    }

    DepotEntity entity = depotMapper.toEntity(request);
    entity.setLocation(location);

    DepotEntity saved = depotRepository.save(entity);
    return enrichResponse(saved);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheConstant.DEPOTS, allEntries = true),
      @CacheEvict(value = CacheConstant.DEPOT_STATS, allEntries = true),
      @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
  })
  public DepotResponse update(Long id, DepotRequest request) {
    authorizationService.requireAdmin();
    DepotEntity entity = depotRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(DepotConstant.DEPOT_NOT_FOUND + id));

    LocationEntity location = locationService.getOrCreateLocation(request.getLocationRequest());
    Long newLocationId = location.getId();

    if (depotRepository.existsByLocation_IdAndIdNot(newLocationId, id)) {
      throw new ValidationException(DepotConstant.DEPOT_LOCATION_EXISTS);
    }

    depotMapper.updateEntityFromRequest(request, entity);
    entity.setLocation(location);

    DepotEntity saved = depotRepository.save(entity);
    return enrichResponse(saved);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheConstant.DEPOTS, allEntries = true),
      @CacheEvict(value = CacheConstant.DEPOT_STATS, allEntries = true),
      @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
  })
  public void delete(Long id) {
    authorizationService.requireAdmin();
    if (!depotRepository.existsById(id)) {
      throw new ResourceNotFoundException(DepotConstant.DEPOT_NOT_FOUND + id);
    }

    if (vehicleRepository.existsByDepot_Id(id)) {
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
    long total;
    long active;
    long inactive;

    if (authorizationService.isAdmin()) {
      total = depotRepository.count();
      active = depotRepository.countByIsActive(true);
      inactive = depotRepository.countByIsActive(false);
    } else {
      Set<Long> accessibleDepotIds = authorizationService.getAccessibleDepotIds();
      total = depotRepository.countByIdIn(accessibleDepotIds);
      active = depotRepository.countByIsActiveAndIdIn(true, accessibleDepotIds);
      inactive = depotRepository.countByIsActiveAndIdIn(false, accessibleDepotIds);
    }

    return new DepotStatisticsResponse(total, active, inactive);
  }

  private DepotResponse enrichResponse(DepotEntity entity) {
    return depotMapper.toResponse(entity);
  }
}
