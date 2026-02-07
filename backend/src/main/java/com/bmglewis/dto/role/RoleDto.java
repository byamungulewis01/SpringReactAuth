package com.bmglewis.dto.role;

import com.bmglewis.dto.permission.PermissionDto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * FILE TYPE: RECORD
 * Role data transfer object
 */
public record RoleDto(
        Long roleId,
        String roleName,
        String description,
        Boolean isActive,
        Set<PermissionDto> permissions,
        LocalDateTime createdAt
) {}