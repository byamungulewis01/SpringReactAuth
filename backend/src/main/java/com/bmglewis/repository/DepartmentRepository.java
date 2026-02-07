package com.bmglewis.repository;

import com.bmglewis.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FILE TYPE: INTERFACE (Repository)
 * Department repository
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {

    Optional<Department> findByDepartmentCode(String departmentCode);

    boolean existsByDepartmentCode(String departmentCode);

    List<Department> findByIsActiveTrue();

    List<Department> findByParentDepartment_DepartmentId(Long parentDepartmentId);

    @Query("SELECT d FROM Department d WHERE d.parentDepartment IS NULL AND d.isActive = true")
    List<Department> findRootDepartments();
}