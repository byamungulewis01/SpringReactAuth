package com.bmglewis.controller;

import com.bmglewis.dto.common.PageResponse;
import com.bmglewis.dto.role.CreateRoleRequest;
import com.bmglewis.dto.role.RoleDto;
import com.bmglewis.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Role management endpoints")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('settings.view')")
    @Operation(summary = "Get all roles with pagination, search, and filters")
    public ResponseEntity<PageResponse<RoleDto>> getAllRoles(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {

        return ResponseEntity.ok(roleService.getAllRoles(
                page, size, sortBy, sortDirection, search, isActive
        ));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('settings.view')")
    @Operation(summary = "Get active roles")
    public ResponseEntity<List<RoleDto>> getActiveRoles() {
        return ResponseEntity.ok(roleService.getActiveRoles());
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('settings.view')")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.getRoleById(roleId));
    }

    @GetMapping("/name/{roleName}")
    @PreAuthorize("hasAuthority('settings.view')")
    @Operation(summary = "Get role by name")
    public ResponseEntity<RoleDto> getRoleByName(@PathVariable String roleName) {
        return ResponseEntity.ok(roleService.getRoleByName(roleName));
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('settings.view')")
    @Operation(summary = "Get role with permissions")
    public ResponseEntity<RoleDto> getRoleWithPermissions(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.getRoleWithPermissions(roleId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('settings.update')")
    @Operation(summary = "Create new role")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleService.createRole(request));
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasAuthority('settings.update')")
    @Operation(summary = "Update role")
    public ResponseEntity<RoleDto> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(roleService.updateRole(roleId, request));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasAuthority('settings.update')")
    @Operation(summary = "Delete role")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{roleId}/deactivate")
    @PreAuthorize("hasAuthority('settings.update')")
    @Operation(summary = "Deactivate role")
    public ResponseEntity<Void> deactivateRole(@PathVariable Long roleId) {
        roleService.deactivateRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{roleId}/activate")
    @PreAuthorize("hasAuthority('settings.update')")
    @Operation(summary = "Activate role")
    public ResponseEntity<Void> activateRole(@PathVariable Long roleId) {
        roleService.activateRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('settings.update')")
    @Operation(summary = "Add permission to role")
    public ResponseEntity<RoleDto> addPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(roleService.addPermissionToRole(roleId, permissionId));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('settings.update')")
    @Operation(summary = "Remove permission from role")
    public ResponseEntity<RoleDto> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(roleService.removePermissionFromRole(roleId, permissionId));
    }
}