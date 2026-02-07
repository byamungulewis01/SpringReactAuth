package com.bmglewis.service;

import com.bmglewis.dto.common.PageResponse;
import com.bmglewis.dto.common.SearchRequest;
import com.bmglewis.dto.department.CreateDepartmentRequest;
import com.bmglewis.dto.department.DepartmentDto;
import com.bmglewis.exception.DuplicateResourceException;
import com.bmglewis.exception.ResourceNotFoundException;
import com.bmglewis.model.Department;
import com.bmglewis.model.User;
import com.bmglewis.repository.DepartmentRepository;
import com.bmglewis.repository.UserRepository;
import com.bmglewis.repository.specification.DepartmentSpecification;
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
 * Department management service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<DepartmentDto> getAllDepartments(
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            String search,
            Boolean isActive) {

        log.debug("Fetching departments - page: {}, size: {}, search: {}", page, size, search);

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

        Page<Department> deptPage = departmentRepository.findAll(
                DepartmentSpecification.withFilters(searchRequest),
                pageable
        );

        Page<DepartmentDto> dtoPage = deptPage.map(this::mapToDto);

        return PageResponse.of(dtoPage);
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getActiveDepartments() {
        log.debug("Fetching active departments");
        return departmentRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long departmentId) {
        log.debug("Fetching department by id: {}", departmentId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        return mapToDto(department);
    }

    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentByCode(String code) {
        log.debug("Fetching department by code: {}", code);
        Department department = departmentRepository.findByDepartmentCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with code: " + code));
        return mapToDto(department);
    }

    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentRequest request) {
        log.info("Creating department: {}", request.departmentCode());

        if (departmentRepository.existsByDepartmentCode(request.departmentCode())) {
            throw new DuplicateResourceException("Department already exists with code: " + request.departmentCode());
        }

        Department department = Department.builder()
                .departmentCode(request.departmentCode())
                .departmentName(request.departmentName())
                .isActive(true)
                .build();

        if (request.parentDepartmentId() != null) {
            Department parent = departmentRepository.findById(request.parentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent department not found"));
            department.setParentDepartment(parent);
        }

        if (request.managerId() != null) {
            User manager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            department.setManager(manager);
        }

        Department saved = departmentRepository.save(department);
        auditService.log("CREATE", "DEPARTMENT", saved.getDepartmentId(),
                "Created department: " + saved.getDepartmentCode());

        log.info("Department created: {}", saved.getDepartmentCode());
        return mapToDto(saved);
    }

    @Transactional
    public DepartmentDto updateDepartment(Long departmentId, CreateDepartmentRequest request) {
        log.info("Updating department: {}", departmentId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        if (request.departmentName() != null) {
            department.setDepartmentName(request.departmentName());
        }

        if (request.parentDepartmentId() != null) {
            Department parent = departmentRepository.findById(request.parentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent department not found"));
            department.setParentDepartment(parent);
        }

        if (request.managerId() != null) {
            User manager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            department.setManager(manager);
        }

        Department updated = departmentRepository.save(department);
        auditService.log("UPDATE", "DEPARTMENT", updated.getDepartmentId(),
                "Updated department: " + updated.getDepartmentCode());

        log.info("Department updated: {}", updated.getDepartmentCode());
        return mapToDto(updated);
    }

    @Transactional
    public void deleteDepartment(Long departmentId) {
        log.info("Deleting department: {}", departmentId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + departmentId));

        departmentRepository.delete(department);
        auditService.log("DELETE", "DEPARTMENT", departmentId,
                "Deleted department: " + department.getDepartmentCode());

        log.info("Department deleted: {}", department.getDepartmentCode());
    }

    private DepartmentDto mapToDto(Department department) {
        return new DepartmentDto(
                department.getDepartmentId(),
                department.getDepartmentCode(),
                department.getDepartmentName(),
                department.getParentDepartment() != null ?
                        department.getParentDepartment().getDepartmentId() : null,
                department.getParentDepartment() != null ?
                        department.getParentDepartment().getDepartmentName() : null,
                department.getManager() != null ? department.getManager().getUserId() : null,
                department.getManager() != null ? department.getManager().getFullName() : null,
                department.getIsActive(),
                department.getCreatedAt()
        );
    }
}