package com.logistics.hub.feature.order.repository;

import com.logistics.hub.common.specification.SpecificationUtils;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<OrderEntity> withFilters(OrderStatus status, String search, Long depotId) {
        return Specification.where(fetchRelations())
                .and(hasStatus(status))
                .and(hasDepotId(depotId))
                .and(matchesSearch(search));
    }

    public static Specification<OrderEntity> withFilters(OrderStatus status, String search, Collection<Long> depotIds) {
        return Specification.where(fetchRelations())
                .and(hasStatus(status))
                .and(hasDepotIds(depotIds))
                .and(matchesSearch(search));
    }

    private static Specification<OrderEntity> fetchRelations() {
        return (root, query, criteriaBuilder) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("deliveryLocation", JoinType.LEFT);
                root.fetch("driver", JoinType.LEFT);
                root.fetch("depot", JoinType.LEFT);
                query.distinct(true);
            }
            return criteriaBuilder.conjunction();
        };
    }

    private static Specification<OrderEntity> hasStatus(OrderStatus status) {
        return SpecificationUtils.equalsValue("status", status);
    }

    private static Specification<OrderEntity> hasDepotId(Long depotId) {
        return SpecificationUtils.equalsValue("depot.id", depotId);
    }

    private static Specification<OrderEntity> hasDepotIds(Collection<Long> depotIds) {
        return (root, query, criteriaBuilder) -> {
            if (depotIds == null || depotIds.isEmpty()) {
                return criteriaBuilder.disjunction();
            }

            return root.get("depot").get("id").in(depotIds);
        };
    }

    private static Specification<OrderEntity> matchesSearch(String search) {
        return SpecificationUtils.anyContainsIgnoreCase(search, "code", "depot.name");
    }
}
