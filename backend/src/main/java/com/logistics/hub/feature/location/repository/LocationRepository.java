package com.logistics.hub.feature.location.repository;

import com.logistics.hub.feature.location.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {
    
    Optional<LocationEntity> findByLatitudeAndLongitude(double latitude, double longitude);
}
