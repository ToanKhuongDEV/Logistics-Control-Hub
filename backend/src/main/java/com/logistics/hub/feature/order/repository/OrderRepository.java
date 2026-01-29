package com.logistics.hub.feature.order.repository;

import com.logistics.hub.feature.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    
    Optional<OrderEntity> findByCode(String code);
    
    boolean existsByCode(String code);
}
