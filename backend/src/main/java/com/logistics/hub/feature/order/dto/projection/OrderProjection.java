package com.logistics.hub.feature.order.dto.projection;

import java.math.BigDecimal;
import java.time.Instant;

public interface OrderProjection {
    Long getId();
    String getCode();
    Long getDeliveryLocationId();
    Integer getWeightKg();
    BigDecimal getVolumeM3();
    String getStatus();
    Instant getCreatedAt();
    
    Long getLocId();
    String getLocName();
}
