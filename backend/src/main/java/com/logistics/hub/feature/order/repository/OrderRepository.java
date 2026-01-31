package com.logistics.hub.feature.order.repository;

import com.logistics.hub.feature.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {
    
    @Query(
        value = "SELECT o.*, l.id as loc_id, l.name as loc_name, l.latitude as loc_lat, l.longitude as loc_lng " +
                "FROM orders o LEFT JOIN locations l ON o.delivery_location_id = l.id " +
                "WHERE o.id = :id",
        nativeQuery = true
    )
    Map<String, Object> findByIdWithLocationData(@Param("id") Long id);
    
    @Query(
        value = "SELECT o.*, l.id as loc_id, l.name as loc_name, l.latitude as loc_lat, l.longitude as loc_lng " +
                "FROM orders o LEFT JOIN locations l ON o.delivery_location_id = l.id",
        nativeQuery = true
    )
    List<Map<String, Object>> findAllWithLocationData();
    
    @Query(
        value = "SELECT o.*, l.id as loc_id, l.name as loc_name, l.latitude as loc_lat, l.longitude as loc_lng " +
                "FROM orders o LEFT JOIN locations l ON o.delivery_location_id = l.id " +
                "WHERE (:status IS NULL OR o.status = :status) " +
                "AND (:search IS NULL OR LOWER(o.code) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                "ORDER BY o.created_at DESC",
        countQuery = "SELECT count(*) FROM orders o " +
                     "WHERE (:status IS NULL OR o.status = :status) " +
                     "AND (:search IS NULL OR LOWER(o.code) LIKE LOWER(CONCAT('%', :search, '%')))",
        nativeQuery = true
    )
    Page<Map<String, Object>> findAllWithLocationAndFilters(
        @Param("status") String status, 
        @Param("search") String search, 
        Pageable pageable
    );
    
    Optional<OrderEntity> findByCode(String code);
    
    boolean existsByCode(String code);
}
