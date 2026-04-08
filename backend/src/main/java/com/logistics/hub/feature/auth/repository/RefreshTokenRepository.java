package com.logistics.hub.feature.auth.repository;

import com.logistics.hub.feature.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

  Optional<RefreshTokenEntity> findByJti(String jti);

  void deleteByUsername(String username);

  void deleteByJti(String jti);
}
