package com.bmglewis.repository;

import com.bmglewis.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FILE TYPE: INTERFACE (Repository)
 * User repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();

    List<User> findByDepartment_DepartmentId(Long departmentId);

    List<User> findByManager_UserId(Long managerId);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.department.departmentId = :departmentId")
    List<User> findActiveUsersByDepartment(@Param("departmentId") Long departmentId);
}