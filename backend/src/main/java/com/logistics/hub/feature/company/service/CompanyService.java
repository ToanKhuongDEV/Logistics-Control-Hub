package com.logistics.hub.feature.company.service;

import com.logistics.hub.feature.company.dto.request.CompanyRequest;
import com.logistics.hub.feature.company.dto.response.CompanyResponse;

public interface CompanyService {
    CompanyResponse getCompanyInfo();
    CompanyResponse createOrUpdateCompanyInfo(CompanyRequest request);
}
