package com.logistics.hub.feature.audit.specification;

import com.logistics.hub.common.specification.SpecificationUtils;
import com.logistics.hub.feature.audit.entity.AuditLogEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class AuditLogSpecification {

    private AuditLogSpecification() {
    }

    public static Specification<AuditLogEntity> withFilters(
            String action,
            String resourceType,
            String actorUsername,
            Long scopeDepotId,
            String status,
            String search,
            Instant from,
            Instant to
    ) {
        return Specification.<AuditLogEntity>where(SpecificationUtils.equalsIgnoreCase("action", action))
                .and(SpecificationUtils.equalsIgnoreCase("resourceType", resourceType))
                .and(SpecificationUtils.containsIgnoreCase("actorUsername", actorUsername))
                .and(SpecificationUtils.equalsValue("scopeDepotId", scopeDepotId))
                .and(SpecificationUtils.equalsIgnoreCase("status", status))
                .and(matchesSearch(search))
                .and(SpecificationUtils.greaterThanOrEqualTo("createdAt", from))
                .and(SpecificationUtils.lessThanOrEqualTo("createdAt", to));
    }

    private static Specification<AuditLogEntity> matchesSearch(String search) {
        return SpecificationUtils.anyContainsIgnoreCase(search, "actorUsername", "resourceType");
    }
}
