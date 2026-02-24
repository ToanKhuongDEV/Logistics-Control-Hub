package com.logistics.hub.feature.vehicle.repository;

import com.logistics.hub.feature.vehicle.entity.VehicleEntity;
import com.logistics.hub.feature.vehicle.enums.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

        Optional<VehicleEntity> findByCode(String code);

        boolean existsByCode(String code);

        @Query("SELECT v FROM VehicleEntity v WHERE " +
                        "(:status IS NULL OR v.status = :status) AND " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(v.code) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<VehicleEntity> findByStatusAndSearch(
                        @Param("status") VehicleStatus status,
                        @Param("search") String search,
                        Pageable pageable);

        @Query("SELECT v.code FROM VehicleEntity v WHERE v.code LIKE CONCAT(:prefix, '%') ORDER BY v.code DESC LIMIT 1")
        String findLatestCodeByPrefix(@Param("prefix") String prefix);

        long countByStatus(VehicleStatus status);

        boolean existsByDriver_Id(Long driverId);

        boolean existsByDriver_IdAndIdNot(Long driverId, Long id);

        boolean existsByDepot_Id(Long depotId);

        @Query("SELECT v FROM VehicleEntity v WHERE v.status = :status AND v.driver IS NOT NULL")
        List<VehicleEntity> findByStatusAndDriverIdNotNull(@Param("status") VehicleStatus status);
}
