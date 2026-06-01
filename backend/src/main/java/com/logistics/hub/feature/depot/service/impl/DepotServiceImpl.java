package com.logistics.hub.feature.depot.service.impl;

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

import java.util.LinkedHashMap;
import java.util.Map;
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
  private final AuditLogService auditLogService;
  private final AuditActorService auditActorService;

  @Override
  @Transactional(readOnly = true)
  public Page<DepotResponse> findAll(String search, Pageable pageable) {
    authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DEPOT_READ);
    Sort sort = Sort.by(
        Sort.Order.desc("isActive"),
        Sort.Order.asc("id"));
    Pageable sortedPageable = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sort);

    Page<DepotEntity> depotPage;
    if (authorizationService.hasGlobalScope()) {
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
    try {
      authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DEPOT_MANAGE);
      LocationEntity location = locationService.getOrCreateLocation(request.getLocationRequest());
      Long locationId = location.getId();

      if (depotRepository.existsByLocation_Id(locationId)) {
        throw new ValidationException(DepotConstant.DEPOT_LOCATION_EXISTS);
      }

      DepotEntity entity = depotMapper.toEntity(request);
      entity.setLocation(location);

      DepotEntity saved = depotRepository.save(entity);
      auditLogService.log(
          auditActorService.getCurrentActor(),
          AuditAction.CREATE,
          AuditResourceType.DEPOT,
          saved.getId().toString(),
          saved.getName(),
          saved.getId(),
          AuditStatus.SUCCESS,
          "Created depot",
          null,
          depotAuditSnapshot(saved),
          null);
      return enrichResponse(saved);
    } catch (RuntimeException ex) {
      logFailure(AuditAction.CREATE, null, request.getName(), null, depotRequestSnapshot(request), ex);
      throw ex;
    }
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheConstant.DEPOTS, allEntries = true),
      @CacheEvict(value = CacheConstant.DEPOT_STATS, allEntries = true),
      @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
  })
  public DepotResponse update(Long id, DepotRequest request) {
    DepotEntity entity = null;
    Map<String, Object> beforeData = null;
    try {
      authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DEPOT_MANAGE);
      entity = depotRepository.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException(DepotConstant.DEPOT_NOT_FOUND + id));
      beforeData = depotAuditSnapshot(entity);

      LocationEntity location = locationService.getOrCreateLocation(request.getLocationRequest());
      Long newLocationId = location.getId();

      if (depotRepository.existsByLocation_IdAndIdNot(newLocationId, id)) {
        throw new ValidationException(DepotConstant.DEPOT_LOCATION_EXISTS);
      }

      depotMapper.updateEntityFromRequest(request, entity);
      entity.setLocation(location);

      DepotEntity saved = depotRepository.save(entity);
      auditLogService.log(
          auditActorService.getCurrentActor(),
          AuditAction.UPDATE,
          AuditResourceType.DEPOT,
          saved.getId().toString(),
          saved.getName(),
          saved.getId(),
          AuditStatus.SUCCESS,
          "Updated depot",
          beforeData,
          depotAuditSnapshot(saved),
          null);
      return enrichResponse(saved);
    } catch (RuntimeException ex) {
      logFailure(AuditAction.UPDATE, id.toString(), entity != null ? entity.getName() : request.getName(), beforeData, depotRequestSnapshot(request), ex);
      throw ex;
    }
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheConstant.DEPOTS, allEntries = true),
      @CacheEvict(value = CacheConstant.DEPOT_STATS, allEntries = true),
      @CacheEvict(value = CacheConstant.DASHBOARD_STATS, allEntries = true)
  })
  public void delete(Long id) {
    DepotEntity depot = null;
    try {
      authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DEPOT_MANAGE);
      depot = depotRepository.findById(id)
          .orElseThrow(() -> new ResourceNotFoundException(DepotConstant.DEPOT_NOT_FOUND + id));

      if (vehicleRepository.existsByDepot_Id(id)) {
        throw new ValidationException(DepotConstant.DEPOT_HAS_VEHICLES);
      }

      try {
        depot.markDeleted();
        depotRepository.save(depot);
        auditLogService.log(
            auditActorService.getCurrentActor(),
            AuditAction.DELETE,
            AuditResourceType.DEPOT,
            depot.getId().toString(),
            depot.getName(),
            depot.getId(),
            AuditStatus.SUCCESS,
            "Deleted depot",
            depotAuditSnapshot(depot),
            null,
            null);
      } catch (DataIntegrityViolationException e) {
        throw new ValidationException(DepotConstant.DEPOT_HAS_VEHICLES);
      }
    } catch (RuntimeException ex) {
      logFailure(AuditAction.DELETE, id.toString(), depot != null ? depot.getName() : null, depot != null ? depotAuditSnapshot(depot) : null, null, ex);
      throw ex;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public DepotStatisticsResponse getStatistics() {
    long total;
    long active;
    long inactive;

    authorizationService.requirePermission(AuthorizationPolicy.PERMISSION_DEPOT_READ);
    if (authorizationService.hasGlobalScope()) {
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

  private Map<String, Object> depotAuditSnapshot(DepotEntity entity) {
    LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("id", entity.getId());
    snapshot.put("name", entity.getName());
    snapshot.put("description", entity.getDescription());
    snapshot.put("isActive", entity.getIsActive());
    snapshot.put("dispatcherId", entity.getDispatcher() != null ? entity.getDispatcher().getId() : null);
    snapshot.put("locationId", entity.getLocation() != null ? entity.getLocation().getId() : null);
    snapshot.put("location", entity.getLocation() == null ? null : Map.of(
        "street", entity.getLocation().getStreet(),
        "city", entity.getLocation().getCity(),
        "country", entity.getLocation().getCountry()));
    return snapshot;
  }

  private Map<String, Object> depotRequestSnapshot(DepotRequest request) {
    LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("name", request.getName());
    snapshot.put("description", request.getDescription());
    snapshot.put("isActive", request.getIsActive());
    snapshot.put("locationRequest", request.getLocationRequest() == null ? null : Map.of(
        "street", request.getLocationRequest().getStreet(),
        "city", request.getLocationRequest().getCity(),
        "country", request.getLocationRequest().getCountry()));
    return snapshot;
  }

  private void logFailure(String action, String resourceId, String resourceName, Object beforeData, Object afterData, RuntimeException ex) {
    if (!(ex instanceof ValidationException || ex instanceof ForbiddenException || ex instanceof ResourceNotFoundException)) {
      return;
    }

    auditLogService.log(
        auditActorService.getCurrentActor(),
        action,
        AuditResourceType.DEPOT,
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
