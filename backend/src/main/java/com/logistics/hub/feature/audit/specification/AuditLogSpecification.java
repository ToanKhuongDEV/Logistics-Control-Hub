package com.logistics.hub.feature.audit.specification;

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
        return Specification.where(equalsIgnoreCase("action", action))
                .and(equalsIgnoreCase("resourceType", resourceType))
                .and(containsIgnoreCase("actorUsername", actorUsername))
                .and(equalsValue("scopeDepotId", scopeDepotId))
                .and(equalsIgnoreCase("status", status))
                .and(matchesSearch(search))
                .and(createdAtFrom(from))
                .and(createdAtTo(to));
    }

    private static Specification<AuditLogEntity> equalsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        return (root, query, cb) -> cb.equal(cb.upper(root.get(field)), normalized);
    }

    private static Specification<AuditLogEntity> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = "%" + value.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), normalized);
    }

    private static Specification<AuditLogEntity> equalsValue(String field, Object value) {
        if (value == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    private static Specification<AuditLogEntity> matchesSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String normalized = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("actorUsername")), normalized),
                cb.like(cb.lower(root.get("resourceType")), normalized),
                cb.like(cb.lower(root.get("resourceId")), normalized),
                cb.like(cb.lower(root.get("resourceName")), normalized),
                cb.like(cb.lower(root.get("message")), normalized),
                cb.like(cb.lower(root.get("requestId")), normalized));
    }

    private static Specification<AuditLogEntity> createdAtFrom(Instant from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    private static Specification<AuditLogEntity> createdAtTo(Instant to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
