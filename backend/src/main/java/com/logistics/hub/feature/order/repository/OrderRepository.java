package com.logistics.hub.feature.order.repository;

import com.logistics.hub.feature.order.dto.projection.OrderProjection;
import com.logistics.hub.feature.order.entity.OrderEntity;
import com.logistics.hub.feature.order.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

        @Query(value = "SELECT o.*, " +
                        "l.id as loc_id, l.street as loc_street, l.city as loc_city, l.country as loc_country, " +
                        "l.latitude as loc_lat, l.longitude as loc_lng, " +
                        "d.id as driver_id, d.name as driver_name, " +
                        "dp.id as depot_id, dp.name as depot_name " +
                        "FROM orders o " +
                        "LEFT JOIN locations l ON o.delivery_location_id = l.id " +
                        "LEFT JOIN drivers d ON o.driver_id = d.id " +
                        "LEFT JOIN depots dp ON o.depot_id = dp.id " +
                        "WHERE o.id = :id", nativeQuery = true)
        Optional<OrderProjection> findByIdWithLocation(@Param("id") Long id);

        @Query(value = "SELECT o.*, " +
                        "l.id as loc_id, l.street as loc_street, l.city as loc_city, l.country as loc_country, " +
                        "l.latitude as loc_lat, l.longitude as loc_lng, " +
                        "d.id as driver_id, d.name as driver_name, " +
                        "dp.id as depot_id, dp.name as depot_name " +
                        "FROM orders o " +
                        "LEFT JOIN locations l ON o.delivery_location_id = l.id " +
                        "LEFT JOIN drivers d ON o.driver_id = d.id " +
                        "LEFT JOIN depots dp ON o.depot_id = dp.id " +
                        "WHERE (:status IS NULL OR o.status = :status) " +
                        "AND (:search IS NULL OR LOWER(o.code) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "ORDER BY o.created_at DESC", countQuery = "SELECT count(*) FROM orders o " +
                                        "WHERE (:status IS NULL OR o.status = :status) " +
                                        "AND (:search IS NULL OR LOWER(o.code) LIKE LOWER(CONCAT('%', :search, '%')))", nativeQuery = true)
        Page<OrderProjection> findAllWithLocationAndFilters(
                        @Param("status") String status,
                        @Param("search") String search,
                        Pageable pageable);

        boolean existsByCode(String code);

        Optional<OrderEntity> findTopByOrderByIdDesc();

        boolean existsByDriver_Id(Long driverId);

        boolean existsByDeliveryLocation_Id(Long locationId);

        Long countByStatus(OrderStatus status);

        List<OrderEntity> findByStatus(OrderStatus status);
}
