package com.logistics.hub.feature.user.mapper;

import com.logistics.hub.feature.auth.dto.response.UserResponse;
import com.logistics.hub.feature.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface UserMapper {

    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "assignedDepots", ignore = true)
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.name")
    UserResponse toResponse(UserEntity entity);
}
