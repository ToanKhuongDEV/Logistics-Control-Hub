package com.logistics.hub.feature.dispatcher.mapper;

import com.logistics.hub.feature.auth.dto.response.DispatcherResponse;
import com.logistics.hub.feature.dispatcher.entity.DispatcherEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface DispatcherMapper {
    
    DispatcherResponse toResponse(DispatcherEntity entity);
}
