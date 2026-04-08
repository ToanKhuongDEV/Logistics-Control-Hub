package com.logistics.hub.feature.vehicle.dto.request;

import com.logistics.hub.feature.vehicle.constant.VehicleConstant;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkVehicleDepotUpdateRequest {

    @NotEmpty(message = VehicleConstant.VEHICLE_IDS_REQUIRED)
    private List<Long> vehicleIds;

    @NotNull(message = VehicleConstant.VEHICLE_DEPOT_ID_REQUIRED)
    private Long depotId;
}
