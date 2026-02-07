package com.logistics.hub.feature.routing.repository;

import com.logistics.hub.feature.routing.entity.RoutingRunEntity;
import com.logistics.hub.feature.routing.enums.RoutingRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutingRunRepository extends JpaRepository<RoutingRunEntity, Long> {

  Long countByStatus(RoutingRunStatus status);
}
