package com.logistics.hub.feature.audit.service;

import com.logistics.hub.feature.audit.dto.response.AuditLogResponse;
import com.logistics.hub.feature.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AuditLogService {

    void log(UserEntity actor,
             String action,
             String resourceType,
             String resourceId,
             String resourceName,
             Long scopeDepotId,
             String status,
             String message,
             Object beforeData,
             Object afterData,
             Object metadata);

    void logByUsername(String actorUsername,
                       String actorRole,
                       String action,
                       String resourceType,
                       String resourceId,
                       String resourceName,
                       Long scopeDepotId,
                       String status,
                       String message,
                       Object beforeData,
                       Object afterData,
                       Object metadata);

    Page<AuditLogResponse> findAll(Pageable pageable,
                                   String action,
                                   String resourceType,
                                   String actorUsername,
                                   Long scopeDepotId,
                                   String status,
                                   String search,
                                   Instant from,
                                   Instant to);
}
