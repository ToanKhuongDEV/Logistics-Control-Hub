package com.logistics.hub.feature.order.repository;

import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<OrderEntity> withFilters(OrderStatus status, String search, Long depotId) {
        return Specification.where(fetchRelations())
                .and(hasStatus(status))
                .and(hasDepotId(depotId))
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
        return (root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
    }

    private static Specification<OrderEntity> hasDepotId(Long depotId) {
        return (root, query, criteriaBuilder) ->
                depotId == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("depot").get("id"), depotId);
    }

    private static Specification<OrderEntity> matchesSearch(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String keyword = "%" + search.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), keyword),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("depot").get("name")), keyword));
        };
    }
}
