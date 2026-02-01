package com.logistics.hub.feature.driver.mapper;

import com.logistics.hub.feature.driver.dto.request.DriverRequest;
import com.logistics.hub.feature.driver.dto.response.DriverResponse;
import com.logistics.hub.feature.driver.entity.DriverEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    DriverResponse toResponse(DriverEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DriverEntity toEntity(DriverRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(DriverRequest request, @MappingTarget DriverEntity entity);
}
