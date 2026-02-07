package com.bmglewis.service;

import com.bmglewis.dto.common.PageResponse;
import com.bmglewis.dto.common.SearchRequest;
import com.bmglewis.dto.permission.PermissionDto;
import com.bmglewis.dto.role.CreateRoleRequest;
import com.bmglewis.dto.role.RoleDto;
import com.bmglewis.exception.DuplicateResourceException;
import com.bmglewis.exception.ResourceNotFoundException;
import com.bmglewis.model.Permission;
import com.bmglewis.model.Role;
import com.bmglewis.repository.PermissionRepository;
import com.bmglewis.repository.RoleRepository;
import com.bmglewis.repository.specification.RoleSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FILE TYPE: CLASS (@Service)
 * Role management service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<RoleDto> getAllRoles(
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            String search,
            Boolean isActive) {

        log.debug("Fetching roles - page: {}, size: {}, search: {}", page, size, search);

        SearchRequest searchRequest = SearchRequest.builder()
                .page(page != null ? page : 0)
                .size(size != null ? size : 10)
                .sortBy(sortBy != null ? sortBy : "createdAt")
                .sortDirection(sortDirection != null ? sortDirection : "DESC")
                .search(search)
                .isActive(isActive)
                .build();

        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                searchRequest.getSortBy()
        );

        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        Page<Role> rolePage = roleRepository.findAll(
                RoleSpecification.withFilters(searchRequest),
                pageable
        );

        Page<RoleDto> dtoPage = rolePage.map(this::mapToDto);

        return PageResponse.of(dtoPage);
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getActiveRoles() {
        log.debug("Fetching active roles");
        return roleRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleDto getRoleById(Long roleId) {
        log.debug("Fetching role by id: {}", roleId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        return mapToDto(role);
    }

    @Transactional(readOnly = true)
    public RoleDto getRoleByName(String roleName) {
        log.debug("Fetching role by name: {}", roleName);
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));
        return mapToDto(role);
    }

    @Transactional(readOnly = true)
    public RoleDto getRoleWithPermissions(Long roleId) {
        log.debug("Fetching role with permissions: {}", roleId);
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        return mapToDto(role);
    }

    @Transactional
    public RoleDto createRole(CreateRoleRequest request) {
        log.info("Creating role: {}", request.roleName());

        if (roleRepository.existsByRoleName(request.roleName())) {
            throw new DuplicateResourceException("Role already exists with name: " + request.roleName());
        }

        Role role = Role.builder()
                .roleName(request.roleName())
                .description(request.description())
                .isActive(true)
                .permissions(new HashSet<>())
                .build();

        // Add permissions if provided
        if (request.permissionIds() != null && !request.permissionIds().isEmpty()) {
            Set<Permission> permissions = request.permissionIds().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        auditService.log("CREATE", "ROLE", savedRole.getRoleId(), "Created role: " + savedRole.getRoleName());

        log.info("Role created: {}", savedRole.getRoleName());
        return mapToDto(savedRole);
    }

    @Transactional
    public RoleDto updateRole(Long roleId, CreateRoleRequest request) {
        log.info("Updating role: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        // Check if name is being changed and if it already exists
        if (request.roleName() != null && !request.roleName().equals(role.getRoleName())) {
            if (roleRepository.existsByRoleName(request.roleName())) {
                throw new DuplicateResourceException("Role already exists with name: " + request.roleName());
            }
            role.setRoleName(request.roleName());
        }

        if (request.description() != null) {
            role.setDescription(request.description());
        }

        // Update permissions if provided
        if (request.permissionIds() != null) {
            Set<Permission> permissions = request.permissionIds().stream()
                    .map(permissionId -> permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }

        Role updatedRole = roleRepository.save(role);
        auditService.log("UPDATE", "ROLE", updatedRole.getRoleId(), "Updated role: " + updatedRole.getRoleName());

        log.info("Role updated: {}", updatedRole.getRoleName());
        return mapToDto(updatedRole);
    }

    @Transactional
    public void deleteRole(Long roleId) {
        log.info("Deleting role: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        roleRepository.delete(role);
        auditService.log("DELETE", "ROLE", roleId, "Deleted role: " + role.getRoleName());

        log.info("Role deleted: {}", role.getRoleName());
    }

    @Transactional
    public void deactivateRole(Long roleId) {
        log.info("Deactivating role: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        role.setIsActive(false);
        roleRepository.save(role);
        auditService.log("DEACTIVATE", "ROLE", roleId, "Deactivated role: " + role.getRoleName());

        log.info("Role deactivated: {}", role.getRoleName());
    }

    @Transactional
    public void activateRole(Long roleId) {
        log.info("Activating role: {}", roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        role.setIsActive(true);
        roleRepository.save(role);
        auditService.log("ACTIVATE", "ROLE", roleId, "Activated role: " + role.getRoleName());

        log.info("Role activated: {}", role.getRoleName());
    }

    @Transactional
    public RoleDto addPermissionToRole(Long roleId, Long permissionId) {
        log.info("Adding permission {} to role {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));

        role.getPermissions().add(permission);
        Role updatedRole = roleRepository.save(role);

        auditService.log("UPDATE", "ROLE", roleId,
                "Added permission: " + permission.getPermissionName() + " to role: " + role.getRoleName());

        log.info("Permission added to role: {}", role.getRoleName());
        return mapToDto(updatedRole);
    }

    @Transactional
    public RoleDto removePermissionFromRole(Long roleId, Long permissionId) {
        log.info("Removing permission {} from role {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));

        role.getPermissions().remove(permission);
        Role updatedRole = roleRepository.save(role);

        auditService.log("UPDATE", "ROLE", roleId,
                "Removed permission: " + permission.getPermissionName() + " from role: " + role.getRoleName());

        log.info("Permission removed from role: {}", role.getRoleName());
        return mapToDto(updatedRole);
    }

    private RoleDto mapToDto(Role role) {
        Set<PermissionDto> permissionDtos = role.getPermissions().stream()
                .map(permission -> new PermissionDto(
                        permission.getPermissionId(),
                        permission.getModule(),
                        permission.getPermissionName(),
                        permission.getDescription()
                ))
                .collect(Collectors.toSet());

        return new RoleDto(
                role.getRoleId(),
                role.getRoleName(),
                role.getDescription(),
                role.getIsActive(),
                permissionDtos,
                role.getCreatedAt()
        );
    }
}