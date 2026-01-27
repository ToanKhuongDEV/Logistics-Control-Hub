package com.logistics.hub.feature.dispatcher.repository;

import com.logistics.hub.feature.dispatcher.entity.DispatcherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DispatcherRepository extends JpaRepository<DispatcherEntity, Long> {
    
    Optional<DispatcherEntity> findByUsername(String username);
    
    boolean existsByUsername(String username);
}
