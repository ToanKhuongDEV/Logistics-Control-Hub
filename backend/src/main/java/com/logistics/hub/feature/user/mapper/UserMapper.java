package com.logistics.hub.feature.user.mapper;

import com.logistics.hub.feature.auth.dto.response.UserResponse;
import com.logistics.hub.feature.user.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface UserMapper {
    
    UserResponse toResponse(UserEntity entity);
}
