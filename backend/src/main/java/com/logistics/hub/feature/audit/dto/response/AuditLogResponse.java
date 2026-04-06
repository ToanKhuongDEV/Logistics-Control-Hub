package com.logistics.hub.feature.audit.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.Instant;

@Data
public class AuditLogResponse {
    private Long id;
    private Long actorUserId;
    private String actorUsername;
    private String actorRole;
    private String action;
    private String resourceType;
    private String resourceId;
    private String resourceName;
    private Long scopeDepotId;
    private String status;
    private String message;
    private JsonNode beforeData;
    private JsonNode afterData;
    private JsonNode metadata;
    private String ipAddress;
    private String userAgent;
    private String requestId;
    private Instant createdAt;
}
