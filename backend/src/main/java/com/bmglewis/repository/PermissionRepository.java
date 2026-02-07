package com.bmglewis.repository;

import com.bmglewis.model.Permission;
import com.bmglewis.model.enums.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FILE TYPE: INTERFACE (Repository)
 * Permission repository
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    Optional<Permission> findByPermissionName(String permissionName);

    List<Permission> findByModule(Module module);

    boolean existsByPermissionName(String permissionName);
}