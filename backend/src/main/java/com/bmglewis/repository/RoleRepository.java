package com.bmglewis.repository;

import com.bmglewis.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FILE TYPE: INTERFACE (Repository)
 * Role repository
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    Optional<Role> findByRoleName(String roleName);

    boolean existsByRoleName(String roleName);

    List<Role> findByIsActiveTrue();

    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.roleId = :roleId")
    Optional<Role> findByIdWithPermissions(@Param("roleId") Long roleId);
}