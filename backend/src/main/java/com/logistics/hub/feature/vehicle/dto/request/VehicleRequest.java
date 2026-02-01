package com.logistics.hub.feature.vehicle.dto.request;

import com.logistics.hub.feature.vehicle.constant.VehicleConstant;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleRequest {
    
    @Size(max = 50, message = "Vehicle code must not exceed 50 characters")
    private String code;
    
    @Min(value = 1, message = VehicleConstant.MAX_WEIGHT_POSITIVE)
    private Integer maxWeightKg;
    
    @Min(value = 1, message = VehicleConstant.MAX_VOLUME_POSITIVE)
    private BigDecimal maxVolumeM3;
    
    @Min(value = 1, message = VehicleConstant.COST_PER_KM_POSITIVE)
    private BigDecimal costPerKm;
    
    @NotNull(message = VehicleConstant.VEHICLE_STATUS_REQUIRED)
    private VehicleStatus status;
    
    @Size(max = 100, message = "Vehicle type must not exceed 100 characters")
    private String type;
    
    private Long driverId;
}
