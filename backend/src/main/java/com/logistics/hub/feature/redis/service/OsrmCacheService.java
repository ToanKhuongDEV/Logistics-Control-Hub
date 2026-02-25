package com.logistics.hub.feature.redis.service;

import com.logistics.hub.feature.location.entity.LocationEntity;
import com.logistics.hub.feature.routing.dto.response.DistanceResult;
import com.logistics.hub.feature.routing.dto.response.MatrixResult;
import com.logistics.hub.feature.routing.service.impl.OsrmDistanceService;
import com.logistics.hub.feature.redis.constant.CacheConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OsrmCacheService {

  private final OsrmDistanceService osrmDistanceService;

  @Cacheable(value = CacheConstant.OSRM_MATRIX, keyGenerator = "osrmMatrixKeyGenerator")
  public MatrixResult getMatrixCached(List<LocationEntity> locations) {
    log.info("[Cache MISS] Fetching OSRM matrix for {} locations", locations.size());
    return osrmDistanceService.getMatrix(locations);
  }

  @Cacheable(value = CacheConstant.OSRM_ROUTE, key = "#origin.id + ':' + #destination.id")
  public DistanceResult getDistanceCached(LocationEntity origin, LocationEntity destination) {
    log.info("[Cache MISS] Fetching OSRM distance: {} -> {}", origin.getId(), destination.getId());
    return osrmDistanceService.getDistanceWithDuration(origin, destination);
  }
}
