package com.logistics.hub.feature.audit.controller;

import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.base.PaginatedResponse;
import com.logistics.hub.common.constant.UrlConstant;
import com.logistics.hub.feature.audit.dto.response.AuditLogResponse;
import com.logistics.hub.feature.audit.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping(UrlConstant.Audit.PREFIX)
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "APIs for tracking user activity across the system")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('audit.read')")
    @Operation(summary = "Get audit logs", description = "Returns a paginated list of audit logs for admin review")
    public ResponseEntity<ApiResponse<PaginatedResponse<AuditLogResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String actorUsername,
            @RequestParam(required = false) Long scopeDepotId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<AuditLogResponse> auditPage = auditLogService.findAll(
                pageable, action, resourceType, actorUsername, scopeDepotId, status, search, from, to);

        PaginatedResponse<AuditLogResponse> response = new PaginatedResponse<>();
        response.setData(auditPage.getContent());
        response.setPagination(new PaginatedResponse.PaginationInfo(
                auditPage.getNumber(),
                auditPage.getSize(),
                auditPage.getTotalElements(),
                auditPage.getTotalPages()));

        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", response));
    }
}
