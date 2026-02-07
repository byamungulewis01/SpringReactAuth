package com.bmglewis.controller;

import com.bmglewis.dto.common.PageResponse;
import com.bmglewis.dto.permission.PermissionDto;
import com.bmglewis.model.enums.Module;
import com.bmglewis.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "Permission Management", description = "Permission management endpoints")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAuthority('settings.view')")
    @Operation(summary = "Get all permissions with pagination, search, and filters")
    public ResponseEntity<PageResponse<PermissionDto>> getAllPermissions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String module) {

        return ResponseEntity.ok(permissionService.getAllPermissions(
                page, size, sortBy, sortDirection, search, module
        ));
    }

    @GetMapping("/module/{module}")
    @PreAuthorize("hasAuthority('settings.view')")
    @Operation(summary = "Get permissions by module")
    public ResponseEntity<List<PermissionDto>> getPermissionsByModule(@PathVariable Module module) {
        return ResponseEntity.ok(permissionService.getPermissionsByModule(module));
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('settings.view')")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<PermissionDto> getPermissionById(@PathVariable Long permissionId) {
        return ResponseEntity.ok(permissionService.getPermissionById(permissionId));
    }

    @GetMapping("/name/{permissionName}")
    @PreAuthorize("hasAuthority('settings.view')")
    @Operation(summary = "Get permission by name")
    public ResponseEntity<PermissionDto> getPermissionByName(@PathVariable String permissionName) {
        return ResponseEntity.ok(permissionService.getPermissionByName(permissionName));
    }
}