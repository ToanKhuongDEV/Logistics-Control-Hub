package com.logistics.hub.feature.driver.repository;

import com.logistics.hub.feature.driver.entity.DriverEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<DriverEntity, Long> {

        boolean existsByLicenseNumber(String licenseNumber);

        boolean existsByPhoneNumber(String phoneNumber);

        boolean existsByLicenseNumberAndIdNot(String licenseNumber, Long id);

        boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

        @Query("SELECT DISTINCT d FROM DriverEntity d " +
                        "LEFT JOIN VehicleEntity v ON v.driver = d " +
                        "WHERE (:search IS NULL OR :search = '' OR " +
                        "LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(d.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:depotId IS NULL OR v.depot.id = :depotId)")
        Page<DriverEntity> findBySearchAndDepot(
                        @Param("search") String search,
                        @Param("depotId") Long depotId,
                        Pageable pageable);

        @Query("SELECT d FROM DriverEntity d WHERE " +
                        "d.id NOT IN (SELECT v.driver.id FROM VehicleEntity v WHERE v.driver IS NOT NULL) " +
                        "OR (:includeDriverId IS NOT NULL AND d.id = :includeDriverId)")
        List<DriverEntity> findAvailableDrivers(@Param("includeDriverId") Long includeDriverId);
}
