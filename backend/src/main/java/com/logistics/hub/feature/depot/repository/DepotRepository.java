package com.logistics.hub.feature.depot.repository;

import com.logistics.hub.feature.depot.entity.DepotEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DepotRepository extends JpaRepository<DepotEntity, Long> {
    boolean existsByLocationId(Long locationId);

    boolean existsByLocationIdAndIdNot(Long locationId, Long id);

    long countByIsActive(Boolean isActive);

    @Query("SELECT d FROM DepotEntity d JOIN LocationEntity l ON d.locationId = l.id "
            + "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(l.street) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(l.country) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<DepotEntity> searchDepots(String search, Pageable pageable);
}
