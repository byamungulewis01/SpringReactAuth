package com.bmglewis.controller;

import com.bmglewis.dto.common.PageResponse;
import com.bmglewis.dto.department.CreateDepartmentRequest;
import com.bmglewis.dto.department.DepartmentDto;
import com.bmglewis.service.DepartmentService;
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
@RequestMapping("/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "Department management endpoints")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get all departments with pagination, search, and filters")
    public ResponseEntity<PageResponse<DepartmentDto>> getAllDepartments(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive) {

        return ResponseEntity.ok(departmentService.getAllDepartments(
                page, size, sortBy, sortDirection, search, isActive
        ));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get active departments")
    public ResponseEntity<List<DepartmentDto>> getActiveDepartments() {
        return ResponseEntity.ok(departmentService.getActiveDepartments());
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long departmentId) {
        return ResponseEntity.ok(departmentService.getDepartmentById(departmentId));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get department by code")
    public ResponseEntity<DepartmentDto> getDepartmentByCode(@PathVariable String code) {
        return ResponseEntity.ok(departmentService.getDepartmentByCode(code));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user.create')")
    @Operation(summary = "Create new department")
    public ResponseEntity<DepartmentDto> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.createDepartment(request));
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasAuthority('user.update')")
    @Operation(summary = "Update department")
    public ResponseEntity<DepartmentDto> updateDepartment(
            @PathVariable Long departmentId,
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(departmentId, request));
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasAuthority('user.delete')")
    @Operation(summary = "Delete department")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long departmentId) {
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.noContent().build();
    }
}