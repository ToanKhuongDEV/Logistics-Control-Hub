package com.logistics.hub.feature.depot.mapper;

import com.logistics.hub.feature.depot.dto.request.DepotRequest;
import com.logistics.hub.feature.depot.dto.response.DepotResponse;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepotMapper {

  @Mapping(target = "address", ignore = true)
  DepotResponse toResponse(DepotEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "locationId", ignore = true)
  DepotEntity toEntity(DepotRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "locationId", ignore = true)
  void updateEntityFromRequest(DepotRequest request, @MappingTarget DepotEntity entity);
}
