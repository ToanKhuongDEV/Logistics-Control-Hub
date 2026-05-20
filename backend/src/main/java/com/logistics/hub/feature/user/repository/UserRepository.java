package com.logistics.hub.feature.user.repository;

import com.logistics.hub.feature.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.assignedDepots LEFT JOIN FETCH u.driver WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithAssignedDepots(String username);

    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.assignedDepots LEFT JOIN FETCH u.driver ORDER BY u.id")
    List<UserEntity> findAllWithAssignedDepots();

    @EntityGraph(attributePaths = {"assignedDepots", "driver"})
    @Query(value = """
            SELECT DISTINCT u
            FROM UserEntity u
            LEFT JOIN u.assignedDepots d
            WHERE (:searchPattern IS NULL OR
                   LOWER(u.username) LIKE :searchPattern OR
                   LOWER(u.fullName) LIKE :searchPattern OR
                   LOWER(u.email) LIKE :searchPattern)
              AND (:role IS NULL OR u.role = :role)
              AND (:depotId IS NULL OR d.id = :depotId)
            ORDER BY u.id
            """,
            countQuery = """
            SELECT COUNT(DISTINCT u.id)
            FROM UserEntity u
            LEFT JOIN u.assignedDepots d
            WHERE (:searchPattern IS NULL OR
                   LOWER(u.username) LIKE :searchPattern OR
                   LOWER(u.fullName) LIKE :searchPattern OR
                   LOWER(u.email) LIKE :searchPattern)
              AND (:role IS NULL OR u.role = :role)
              AND (:depotId IS NULL OR d.id = :depotId)
            """)
    Page<UserEntity> searchAccounts(String searchPattern, String role, Long depotId, Pageable pageable);

    @Query("SELECT DISTINCT u FROM UserEntity u LEFT JOIN FETCH u.assignedDepots LEFT JOIN FETCH u.driver WHERE u.id = :id")
    Optional<UserEntity> findByIdWithAssignedDepots(Long id);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDriver_Id(Long driverId);

    boolean existsByDriver_IdAndIdNot(Long driverId, Long id);

    long countByRoleIgnoreCase(String role);
}
