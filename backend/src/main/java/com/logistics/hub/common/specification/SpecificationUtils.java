package com.logistics.hub.common.specification;

import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

public final class SpecificationUtils {

    private SpecificationUtils() {
    }

    public static <T> Specification<T> equalsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase();
        return (root, query, cb) -> cb.equal(cb.upper(resolvePath(root, field).as(String.class)), normalized);
    }

    public static <T> Specification<T> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = "%" + value.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(resolvePath(root, field).as(String.class)), normalized);
    }

    public static <T> Specification<T> equalsValue(String field, Object value) {
        if (value == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(resolvePath(root, field), value);
    }

    public static <T, Y extends Comparable<? super Y>> Specification<T> greaterThanOrEqualTo(String field, Y value) {
        if (value == null) {
            return null;
        }

        return (root, query, cb) -> cb.greaterThanOrEqualTo(resolveComparablePath(root, field), value);
    }

    public static <T, Y extends Comparable<? super Y>> Specification<T> lessThanOrEqualTo(String field, Y value) {
        if (value == null) {
            return null;
        }

        return (root, query, cb) -> cb.lessThanOrEqualTo(resolveComparablePath(root, field), value);
    }

    public static <T> Specification<T> anyContainsIgnoreCase(String search, String... fields) {
        if (search == null || search.isBlank() || fields == null || fields.length == 0) {
            return null;
        }

        String normalized = "%" + search.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                java.util.Arrays.stream(fields)
                        .map(field -> cb.like(cb.lower(resolvePath(root, field).as(String.class)), normalized))
                        .toArray(jakarta.persistence.criteria.Predicate[]::new)
        );
    }

    private static <T> Path<?> resolvePath(Path<T> root, String field) {
        Path<?> path = root;
        for (String part : field.split("\\.")) {
            path = path.get(part);
        }
        return path;
    }

    @SuppressWarnings("unchecked")
    private static <Y extends Comparable<? super Y>> Path<Y> resolveComparablePath(Path<?> root, String field) {
        return (Path<Y>) resolvePath(root, field);
    }
}
