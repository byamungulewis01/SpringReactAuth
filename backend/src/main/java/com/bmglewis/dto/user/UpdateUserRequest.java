package com.bmglewis.dto.user;

import jakarta.validation.constraints.Email;

import java.util.Set;

/**
 * FILE TYPE: RECORD
 * Request to update user
 */
public record UpdateUserRequest(
        @Email(message = "Email must be valid")
        String email,
        String firstName,
        String lastName,
        String phone,
        Long departmentId,
        Long managerId,
        Boolean isActive,
        Set<Long> roleIds
) {}