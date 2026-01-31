package com.logistics.hub.feature.company.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.feature.company.dto.request.CompanyRequest;
import com.logistics.hub.feature.company.dto.response.CompanyResponse;
import com.logistics.hub.feature.company.service.CompanyService;
import com.logistics.hub.feature.company.constant.CompanyConstant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.logistics.hub.common.constant.UrlConstant;

@RestController
@RequestMapping(UrlConstant.Company.PREFIX)
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyInfo() {
        CompanyResponse response = companyService.getCompanyInfo();
        return ResponseEntity.ok(ApiResponse.success(CompanyConstant.COMPANY_INFO_RETRIEVED_SUCCESS, response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> createOrUpdateCompanyInfo(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.createOrUpdateCompanyInfo(request);
        return ResponseEntity.ok(ApiResponse.success(CompanyConstant.COMPANY_INFO_SAVED_SUCCESS, response));
    }
}
