package com.logistics.hub.feature.audit.context;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditRequestContext {
    private final String requestId;
    private final String ipAddress;
    private final String userAgent;
}
