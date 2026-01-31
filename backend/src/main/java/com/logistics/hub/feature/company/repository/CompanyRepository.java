package com.logistics.hub.feature.company.repository;

import com.logistics.hub.feature.company.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    // Helper to find the single record since we likely only have one company
    Optional<CompanyEntity> findTopByOrderByIdAsc();
}
