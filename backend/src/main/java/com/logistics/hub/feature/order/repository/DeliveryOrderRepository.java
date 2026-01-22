package com.logistics.hub.feature.order.repository;

import com.logistics.hub.feature.order.entity.DeliveryOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrderEntity, Long> {
    Optional<DeliveryOrderEntity> findByOrderNumber(String orderNumber);
}
