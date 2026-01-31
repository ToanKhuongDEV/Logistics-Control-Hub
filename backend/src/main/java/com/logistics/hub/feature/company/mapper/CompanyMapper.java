package com.logistics.hub.feature.company.mapper;

import com.logistics.hub.feature.company.dto.request.CompanyRequest;
import com.logistics.hub.feature.company.dto.response.CompanyResponse;
import com.logistics.hub.feature.company.entity.CompanyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    CompanyEntity toEntity(CompanyRequest request);
    CompanyResponse toResponse(CompanyEntity entity);
    void updateEntityFromRequest(CompanyRequest request, @MappingTarget CompanyEntity entity);
}
