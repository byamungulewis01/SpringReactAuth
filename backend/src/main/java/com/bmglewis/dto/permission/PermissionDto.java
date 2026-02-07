package com.bmglewis.dto.permission;

import com.bmglewis.model.enums.Module;

/**
 * FILE TYPE: RECORD
 * Permission data transfer object
 */
public record PermissionDto(
        Long permissionId,
        Module module,
        String permissionName,
        String description
) {}