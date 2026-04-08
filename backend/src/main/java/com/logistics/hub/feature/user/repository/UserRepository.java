package com.logistics.hub.feature.user.repository;

import com.logistics.hub.feature.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.assignedDepots WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithAssignedDepots(String username);

    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.assignedDepots ORDER BY u.id")
    List<UserEntity> findAllWithAssignedDepots();

    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.assignedDepots WHERE u.id = :id")
    Optional<UserEntity> findByIdWithAssignedDepots(Long id);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);
}
