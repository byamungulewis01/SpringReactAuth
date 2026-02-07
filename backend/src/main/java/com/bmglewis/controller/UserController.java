package com.bmglewis.controller;

import com.bmglewis.dto.common.PageResponse;
import com.bmglewis.dto.user.CreateUserRequest;
import com.bmglewis.dto.user.UpdateUserRequest;
import com.bmglewis.dto.user.UserDto;
import com.bmglewis.service.UserService;
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
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management endpoints")
public class UserController {

    private final UserService userService;

//    @GetMapping
//    @PreAuthorize("hasAuthority('user.read')")
//    @Operation(summary = "Get all users")
//    public ResponseEntity<List<UserDto>> getAllUsers() {
//        return ResponseEntity.ok(userService.getAllUsers());
//    }

    @GetMapping
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get all users with pagination, search, and filters")
    public ResponseEntity<PageResponse<UserDto>> getAllUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) String roleName) {

        return ResponseEntity.ok(userService.getAllUsers(
                page, size, sortBy, sortDirection, search, isActive, departmentId, managerId, roleName
        ));
    }


    @GetMapping("/active")
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get active users")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get user by email")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get users by department")
    public ResponseEntity<List<UserDto>> getUsersByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(userService.getUsersByDepartment(departmentId));
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get users by manager")
    public ResponseEntity<List<UserDto>> getUsersByManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(userService.getUsersByManager(managerId));
    }

    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasAuthority('user.read')")
    @Operation(summary = "Get users by role")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String roleName) {
        return ResponseEntity.ok(userService.getUsersByRole(roleName));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user.create')")
    @Operation(summary = "Create new user")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAuthority('user.update')")
    @Operation(summary = "Update user")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user.delete')")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasAuthority('user.update')")
    @Operation(summary = "Deactivate user")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasAuthority('user.update')")
    @Operation(summary = "Activate user")
    public ResponseEntity<Void> activateUser(@PathVariable Long userId) {
        userService.activateUser(userId);
        return ResponseEntity.noContent().build();
    }
}