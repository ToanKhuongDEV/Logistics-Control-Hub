package com.logistics.hub.feature.dispatcher.repository;

import com.logistics.hub.feature.dispatcher.entity.DispatcherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DispatcherRepository extends JpaRepository<DispatcherEntity, Long> {
    
    Optional<DispatcherEntity> findByUsername(String username);

    Optional<DispatcherEntity> findByEmailIgnoreCase(String email);
    
    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);
}
