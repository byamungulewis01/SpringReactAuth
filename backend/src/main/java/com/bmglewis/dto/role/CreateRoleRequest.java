package com.bmglewis.dto.role;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

/**
 * FILE TYPE: RECORD
 * Request to create a role
 */
public record CreateRoleRequest(
        @NotBlank(message = "Role name is required")
        String roleName,
        String description,
        Set<Long> permissionIds
) {}