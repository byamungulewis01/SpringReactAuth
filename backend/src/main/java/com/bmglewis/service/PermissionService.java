package com.bmglewis.service;

import com.bmglewis.dto.common.PageResponse;
import com.bmglewis.dto.common.SearchRequest;
import com.bmglewis.dto.permission.PermissionDto;
import com.bmglewis.model.Permission;
import com.bmglewis.model.enums.Module;
import com.bmglewis.repository.PermissionRepository;
import com.bmglewis.repository.specification.PermissionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FILE TYPE: CLASS (@Service)
 * Permission management service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public PageResponse<PermissionDto> getAllPermissions(
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            String search,
            String module) {

        log.debug("Fetching permissions - page: {}, size: {}, search: {}", page, size, search);

        SearchRequest searchRequest = SearchRequest.builder()
                .page(page != null ? page : 0)
                .size(size != null ? size : 10)
                .sortBy(sortBy != null ? sortBy : "createdAt")
                .sortDirection(sortDirection != null ? sortDirection : "DESC")
                .search(search)
                .module(module)
                .build();

        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                searchRequest.getSortBy()
        );

        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        Page<Permission> permPage = permissionRepository.findAll(
                PermissionSpecification.withFilters(searchRequest),
                pageable
        );

        Page<PermissionDto> dtoPage = permPage.map(this::mapToDto);

        return PageResponse.of(dtoPage);
    }

    @Transactional(readOnly = true)
    public List<PermissionDto> getPermissionsByModule(Module module) {
        log.debug("Fetching permissions by module: {}", module);
        return permissionRepository.findByModule(module).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PermissionDto getPermissionById(Long permissionId) {
        log.debug("Fetching permission by id: {}", permissionId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new com.bmglewis.exception.ResourceNotFoundException(
                        "Permission not found with id: " + permissionId));
        return mapToDto(permission);
    }

    @Transactional(readOnly = true)
    public PermissionDto getPermissionByName(String permissionName) {
        log.debug("Fetching permission by name: {}", permissionName);
        Permission permission = permissionRepository.findByPermissionName(permissionName)
                .orElseThrow(() -> new com.bmglewis.exception.ResourceNotFoundException(
                        "Permission not found with name: " + permissionName));
        return mapToDto(permission);
    }

    private PermissionDto mapToDto(Permission permission) {
        return new PermissionDto(
                permission.getPermissionId(),
                permission.getModule(),
                permission.getPermissionName(),
                permission.getDescription()
        );
    }
}