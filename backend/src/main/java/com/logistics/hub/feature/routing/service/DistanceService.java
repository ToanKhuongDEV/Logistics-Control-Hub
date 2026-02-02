package com.logistics.hub.feature.routing.service;

import com.logistics.hub.feature.location.entity.LocationEntity;
import java.math.BigDecimal;

public interface DistanceService {
    BigDecimal getDistanceKm(LocationEntity origin, LocationEntity destination);
}
