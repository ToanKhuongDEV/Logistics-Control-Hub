package com.logistics.hub.feature.company.service.impl;

import com.logistics.hub.feature.audit.constant.AuditAction;
import com.logistics.hub.feature.audit.constant.AuditResourceType;
import com.logistics.hub.feature.audit.constant.AuditStatus;
import com.logistics.hub.feature.audit.service.AuditActorService;
import com.logistics.hub.feature.audit.service.AuditLogService;
import com.logistics.hub.feature.company.dto.request.CompanyRequest;
import com.logistics.hub.feature.company.dto.response.CompanyResponse;
import com.logistics.hub.feature.company.entity.CompanyEntity;
import com.logistics.hub.feature.company.mapper.CompanyMapper;
import com.logistics.hub.feature.company.repository.CompanyRepository;
import com.logistics.hub.feature.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final AuditLogService auditLogService;
    private final AuditActorService auditActorService;

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
        boolean isCreate = entity.getId() == null;
        Map<String, Object> beforeData = isCreate ? null : companyAuditSnapshot(entity);

        if (isCreate) {
            // New entity mapping
            entity = companyMapper.toEntity(request);
        } else {
            // Update existing
            companyMapper.updateEntityFromRequest(request, entity);
        }

        CompanyEntity saved = companyRepository.save(entity);
        auditLogService.log(
                auditActorService.getCurrentActor(),
                isCreate ? AuditAction.CREATE : AuditAction.UPDATE,
                AuditResourceType.COMPANY,
                saved.getId().toString(),
                saved.getName(),
                null,
                AuditStatus.SUCCESS,
                isCreate ? "Created company profile" : "Updated company profile",
                beforeData,
                companyAuditSnapshot(saved),
                null);
        return companyMapper.toResponse(saved);
    }

    private Map<String, Object> companyAuditSnapshot(CompanyEntity entity) {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", entity.getId());
        snapshot.put("name", entity.getName());
        snapshot.put("address", entity.getAddress());
        snapshot.put("phone", entity.getPhone());
        snapshot.put("email", entity.getEmail());
        snapshot.put("website", entity.getWebsite());
        snapshot.put("taxId", entity.getTaxId());
        snapshot.put("description", entity.getDescription());
        return snapshot;
    }
}
