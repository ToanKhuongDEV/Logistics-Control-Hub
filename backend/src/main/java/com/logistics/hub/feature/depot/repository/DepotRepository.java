package com.logistics.hub.feature.depot.repository;

import com.logistics.hub.feature.depot.entity.DepotEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DepotRepository extends JpaRepository<DepotEntity, Long>, JpaSpecificationExecutor<DepotEntity> {
    List<DepotEntity> findByDispatcher_Id(Long dispatcherId);

    boolean existsByLocation_Id(Long locationId);

    boolean existsByLocation_IdAndIdNot(Long locationId, Long id);

    Long countByIsActive(boolean isActive);

    @Query("SELECT d FROM DepotEntity d JOIN d.location l "
            + "WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(l.street) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(l.country) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<DepotEntity> searchDepots(String search, Pageable pageable);

    Page<DepotEntity> findByIdIn(Collection<Long> depotIds, Pageable pageable);

    @Query("SELECT d FROM DepotEntity d JOIN d.location l "
            + "WHERE d.id IN :depotIds AND ("
            + "LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(l.street) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(l.country) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DepotEntity> searchDepotsByIds(String search, Collection<Long> depotIds, Pageable pageable);

    Long countByIdIn(Collection<Long> depotIds);

    Long countByIsActiveAndIdIn(boolean isActive, Collection<Long> depotIds);
}
