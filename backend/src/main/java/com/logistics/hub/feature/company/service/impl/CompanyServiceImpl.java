package com.logistics.hub.feature.company.service.impl;

import com.logistics.hub.feature.company.dto.request.CompanyRequest;
import com.logistics.hub.feature.company.dto.response.CompanyResponse;
import com.logistics.hub.feature.company.entity.CompanyEntity;
import com.logistics.hub.feature.company.mapper.CompanyMapper;
import com.logistics.hub.feature.company.repository.CompanyRepository;
import com.logistics.hub.feature.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyInfo() {
        Optional<CompanyEntity> entity = companyRepository.findTopByOrderByIdAsc();
        return entity.map(companyMapper::toResponse).orElse(null);
    }

    @Override
    public CompanyResponse createOrUpdateCompanyInfo(CompanyRequest request) {
        CompanyEntity entity = companyRepository.findTopByOrderByIdAsc()
                .orElse(new CompanyEntity());

        if (entity.getId() == null) {
            // New entity mapping
            entity = companyMapper.toEntity(request);
        } else {
            // Update existing
            companyMapper.updateEntityFromRequest(request, entity);
        }

        CompanyEntity saved = companyRepository.save(entity);
        return companyMapper.toResponse(saved);
    }
}
