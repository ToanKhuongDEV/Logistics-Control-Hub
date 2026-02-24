package com.logistics.hub.feature.order.mapper;

import com.logistics.hub.feature.order.dto.request.OrderRequest;
import com.logistics.hub.feature.order.dto.response.OrderResponse;
import com.logistics.hub.feature.order.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "deliveryLocationName", ignore = true)
    @Mapping(target = "driverName", ignore = true)
    @Mapping(target = "deliveryStreet", ignore = true)
    @Mapping(target = "deliveryCity", ignore = true)
    @Mapping(target = "deliveryCountry", ignore = true)
    @Mapping(target = "driverId", ignore = true)
    OrderResponse toResponse(OrderEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deliveryLocation", ignore = true)
    @Mapping(target = "driver", ignore = true)
    OrderEntity toEntity(OrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deliveryLocation", ignore = true)
    @Mapping(target = "driver", ignore = true)
    void updateEntityFromRequest(OrderRequest request, @MappingTarget OrderEntity entity);
}
