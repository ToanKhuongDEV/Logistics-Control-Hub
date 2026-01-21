package com.logistics.hub.feature.location.mapper;

import com.logistics.hub.feature.location.dto.LocationDTO;
import com.logistics.hub.feature.location.entity.LocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    
    LocationDTO toDTO(LocationEntity entity);
    
    LocationEntity toEntity(LocationDTO dto);
    
    void updateEntityFromDTO(LocationDTO dto, @MappingTarget LocationEntity entity);
}
