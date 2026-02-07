package com.bmglewis.dto.user;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * FILE TYPE: RECORD
 * User data transfer object
 */
public record UserDto(
        Long userId,
        String email,
        String firstName,
        String lastName,
        String phone,
        Long departmentId,
        String departmentName,
        Long managerId,
        String managerName,
        Boolean isActive,
        LocalDateTime lastLoginAt,
        Set<String> roles,
        LocalDateTime createdAt
) {}