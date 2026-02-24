package com.logistics.hub.feature.vehicle.mapper;

import com.logistics.hub.feature.vehicle.dto.request.VehicleRequest;
import com.logistics.hub.feature.vehicle.dto.response.VehicleResponse;
import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "depotId", source = "depot.id")
    @Mapping(target = "driverName", source = "driver.name")
    @Mapping(target = "depotName", source = "depot.name")
    @Mapping(target = "locationId", source = "depot.location.id")
    @Mapping(target = "street", source = "depot.location.street")
    @Mapping(target = "city", source = "depot.location.city")
    @Mapping(target = "country", source = "depot.location.country")
    VehicleResponse toResponse(VehicleEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "depot", ignore = true)
    VehicleEntity toEntity(VehicleRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "depot", ignore = true)
    void updateEntityFromRequest(VehicleRequest request, @MappingTarget VehicleEntity entity);
}
