package com.logistics.hub.feature.depot.mapper;

import com.logistics.hub.feature.depot.dto.request.DepotRequest;
import com.logistics.hub.feature.depot.dto.response.DepotResponse;
import com.logistics.hub.feature.depot.entity.DepotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepotMapper {

  @Mapping(target = "locationId", source = "location.id")
  @Mapping(target = "street", source = "location.street")
  @Mapping(target = "city", source = "location.city")
  @Mapping(target = "country", source = "location.country")
  DepotResponse toResponse(DepotEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "location", ignore = true)
  DepotEntity toEntity(DepotRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "location", ignore = true)
  void updateEntityFromRequest(DepotRequest request, @MappingTarget DepotEntity entity);
}
