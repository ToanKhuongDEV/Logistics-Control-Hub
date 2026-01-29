package com.logistics.hub.feature.location.service;

import com.logistics.hub.feature.location.dto.request.LocationRequest;
import com.logistics.hub.feature.location.dto.response.LocationResponse;
import com.logistics.hub.feature.location.entity.LocationEntity;

public interface LocationService {
    
    LocationEntity getOrCreateLocation(LocationRequest request);

    LocationResponse create(LocationRequest request);

    void delete(Long id);
}
