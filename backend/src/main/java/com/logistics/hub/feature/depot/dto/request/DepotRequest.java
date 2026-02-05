package com.logistics.hub.feature.depot.dto.request;

import com.logistics.hub.feature.depot.constant.DepotConstant;
import com.logistics.hub.feature.location.dto.request.LocationRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepotRequest {

  @NotBlank(message = DepotConstant.DEPOT_NAME_REQUIRED)
  @Size(max = 255, message = DepotConstant.DEPOT_NAME_LENGTH_EXCEEDED)
  private String name;

  @NotNull(message = DepotConstant.DEPOT_LOCATION_REQUIRED)
  @Valid
  private LocationRequest locationRequest;

  @Size(max = 500, message = DepotConstant.DEPOT_DESCRIPTION_LENGTH_EXCEEDED)
  private String description;

  private Boolean isActive = true;
}
