package com.logistics.hub.feature.vehicle.dto.response;

import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class VehicleResponse {
    private Long id;
    private String code;
    private Integer maxWeightKg;
    private BigDecimal maxVolumeM3;
    private BigDecimal costPerKm;
    private VehicleStatus status;
    private String type;
    private Long driverId;
    private String driverName;
    private Long depotId;
    private String depotName;
    private Long locationId;
    private String street;
    private String city;
    private String country;
    private Instant createdAt;
}
