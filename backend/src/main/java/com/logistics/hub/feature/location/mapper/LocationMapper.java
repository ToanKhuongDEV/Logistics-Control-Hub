package com.logistics.hub.feature.location.mapper;

import com.logistics.hub.feature.location.dto.response.LocationResponse;
import com.logistics.hub.feature.location.entity.LocationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    
    LocationResponse toResponse(LocationEntity entity);
}
