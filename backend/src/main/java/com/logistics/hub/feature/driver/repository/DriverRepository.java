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



    @Query("SELECT d FROM DriverEntity d WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(d.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DriverEntity> findBySearch(
        @Param("search") String search,
        Pageable pageable
    );
    @Query("SELECT d FROM DriverEntity d WHERE " +
           "d.id NOT IN (SELECT v.driverId FROM VehicleEntity v WHERE v.driverId IS NOT NULL) " +
           "OR (:includeDriverId IS NOT NULL AND d.id = :includeDriverId)")
    List<DriverEntity> findAvailableDrivers(@Param("includeDriverId") Long includeDriverId);
}
