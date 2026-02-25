package com.logistics.hub.feature.redis.service;

import com.logistics.hub.feature.redis.constant.CacheConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheEvictService {

  private final CacheManager cacheManager;

  public void evictAllOsrmCache() {
    evictCache(CacheConstant.OSRM_MATRIX);
    evictCache(CacheConstant.OSRM_ROUTE);
    log.info("All OSRM caches evicted");
  }

  public void evictMatrixCache() {
    evictCache(CacheConstant.OSRM_MATRIX);
    log.info("OSRM matrix cache evicted");
  }

  public void evictRouteCache() {
    evictCache(CacheConstant.OSRM_ROUTE);
    log.info("OSRM route cache evicted");
  }

  private void evictCache(String cacheName) {
    var cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.clear();
    }
  }
}
