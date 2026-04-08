package com.logistics.hub.feature.audit.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.hub.feature.audit.context.AuditRequestContext;
import com.logistics.hub.feature.audit.context.AuditRequestContextHolder;
import com.logistics.hub.feature.audit.dto.response.AuditLogResponse;
import com.logistics.hub.feature.audit.entity.AuditLogEntity;
import com.logistics.hub.feature.audit.repository.AuditLogRepository;
import com.logistics.hub.feature.audit.service.AuditLogService;
import com.logistics.hub.feature.audit.specification.AuditLogSpecification;
import com.logistics.hub.feature.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UserEntity actor, String action, String resourceType, String resourceId, String resourceName,
                    Long scopeDepotId, String status, String message, Object beforeData, Object afterData, Object metadata) {
        try {
            AuditLogEntity entity = baseEntity(action, resourceType, resourceId, resourceName, scopeDepotId, status, message, beforeData, afterData, metadata);
            if (actor != null) {
                entity.setActorUser(actor);
                entity.setActorUsername(actor.getUsername());
                entity.setActorRole(actor.getRole());
            }
            auditLogRepository.save(entity);
        } catch (Exception ex) {
            log.error("Failed to persist audit log for action={} resourceType={} resourceId={}", action, resourceType, resourceId, ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logByUsername(String actorUsername, String actorRole, String action, String resourceType, String resourceId,
                              String resourceName, Long scopeDepotId, String status, String message,
                              Object beforeData, Object afterData, Object metadata) {
        try {
            AuditLogEntity entity = baseEntity(action, resourceType, resourceId, resourceName, scopeDepotId, status, message, beforeData, afterData, metadata);
            entity.setActorUsername(actorUsername);
            entity.setActorRole(actorRole);
            auditLogRepository.save(entity);
        } catch (Exception ex) {
            log.error("Failed to persist audit log for actorUsername={} action={} resourceType={}", actorUsername, action, resourceType, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAll(Pageable pageable, String action, String resourceType, String actorUsername,
                                          Long scopeDepotId, String status, String search, Instant from, Instant to) {
        return auditLogRepository.findAll(
                        AuditLogSpecification.withFilters(action, resourceType, actorUsername, scopeDepotId, status, search, from, to),
                        pageable)
                .map(this::toResponse);
    }

    private AuditLogEntity baseEntity(String action, String resourceType, String resourceId, String resourceName,
                                      Long scopeDepotId, String status, String message,
                                      Object beforeData, Object afterData, Object metadata) {
        AuditRequestContext requestContext = AuditRequestContextHolder.get();

        AuditLogEntity entity = new AuditLogEntity();
        entity.setAction(action);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);
        entity.setResourceName(resourceName);
        entity.setScopeDepotId(scopeDepotId);
        entity.setStatus(status);
        entity.setMessage(message);
        entity.setBeforeData(toJsonNode(beforeData));
        entity.setAfterData(toJsonNode(afterData));
        entity.setMetadata(toJsonNode(metadata));

        if (requestContext != null) {
            entity.setRequestId(requestContext.getRequestId());
            entity.setIpAddress(requestContext.getIpAddress());
            entity.setUserAgent(requestContext.getUserAgent());
        }
        return entity;
    }

    private JsonNode toJsonNode(Object value) {
        return value == null ? null : objectMapper.valueToTree(value);
    }

    private AuditLogResponse toResponse(AuditLogEntity entity) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(entity.getId());
        response.setActorUserId(entity.getActorUser() != null ? entity.getActorUser().getId() : null);
        response.setActorUsername(entity.getActorUsername());
        response.setActorRole(entity.getActorRole());
        response.setAction(entity.getAction());
        response.setResourceType(entity.getResourceType());
        response.setResourceId(entity.getResourceId());
        response.setResourceName(entity.getResourceName());
        response.setScopeDepotId(entity.getScopeDepotId());
        response.setStatus(entity.getStatus());
        response.setMessage(entity.getMessage());
        response.setBeforeData(entity.getBeforeData());
        response.setAfterData(entity.getAfterData());
        response.setMetadata(entity.getMetadata());
        response.setIpAddress(entity.getIpAddress());
        response.setUserAgent(entity.getUserAgent());
        response.setRequestId(entity.getRequestId());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
