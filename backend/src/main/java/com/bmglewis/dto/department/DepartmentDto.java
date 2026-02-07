package com.bmglewis.dto.department;

import java.time.LocalDateTime;

/**
 * FILE TYPE: RECORD
 * Department data transfer object
 */
public record DepartmentDto(
        Long departmentId,
        String departmentCode,
        String departmentName,
        Long parentDepartmentId,
        String parentDepartmentName,
        Long managerId,
        String managerName,
        Boolean isActive,
        LocalDateTime createdAt
) {}