package com.bmglewis.dto.department;

import jakarta.validation.constraints.NotBlank;

/**
 * FILE TYPE: RECORD
 * Request to create a department
 */
public record CreateDepartmentRequest(
        @NotBlank(message = "Department code is required")
        String departmentCode,

        @NotBlank(message = "Department name is required")
        String departmentName,

        Long parentDepartmentId,
        Long managerId
) {}