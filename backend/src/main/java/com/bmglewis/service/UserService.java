package com.bmglewis.service;

import com.bmglewis.dto.common.PageResponse;
import com.bmglewis.dto.common.SearchRequest;
import com.bmglewis.repository.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.bmglewis.dto.user.CreateUserRequest;
import com.bmglewis.dto.user.UpdateUserRequest;
import com.bmglewis.dto.user.UserDto;
import com.bmglewis.exception.DuplicateResourceException;
import com.bmglewis.exception.ResourceNotFoundException;
import com.bmglewis.model.Department;
import com.bmglewis.model.Role;
import com.bmglewis.model.User;
import com.bmglewis.repository.DepartmentRepository;
import com.bmglewis.repository.RoleRepository;
import com.bmglewis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FILE TYPE: CLASS (@Service)
 * User management service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

//    @Transactional(readOnly = true)
//    public List<UserDto> getAllUsers() {
//        log.debug("Fetching all users");
//        return userRepository.findAll().stream()
//                .map(this::mapToDto)
//                .collect(Collectors.toList());
//    }


    @Transactional(readOnly = true)
    public PageResponse<UserDto> getAllUsers(
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection,
            String search,
            Boolean isActive,
            Long departmentId,
            Long managerId,
            String roleName) {

        log.debug("Fetching users - page: {}, size: {}, search: {}", page, size, search);

        // Build search request
        SearchRequest searchRequest = SearchRequest.builder()
                .page(page != null ? page : 0)
                .size(size != null ? size : 10)
                .sortBy(sortBy != null ? sortBy : "createdAt")
                .sortDirection(sortDirection != null ? sortDirection : "DESC")
                .search(search)
                .isActive(isActive)
                .departmentId(departmentId)
                .managerId(managerId)
                .roleName(roleName)
                .build();

        // Create sort
        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                searchRequest.getSortBy()
        );

        // Create pageable
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        // Execute search with specifications
        Page<User> userPage = userRepository.findAll(
                UserSpecification.withFilters(searchRequest),
                pageable
        );

        // Map to DTOs
        Page<UserDto> dtoPage = userPage.map(this::mapToDto);

        return PageResponse.of(dtoPage);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getActiveUsers() {
        log.debug("Fetching active users");
        return userRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.debug("Fetching user by id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToDto(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating user: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User already exists with email: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .isActive(true)
                .build();

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            user.setDepartment(department);
        }

        if (request.managerId() != null) {
            User manager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            user.setManager(manager);
        }

        if (request.roleIds() != null && !request.roleIds().isEmpty()) {
            Set<Role> roles = request.roleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        auditService.log("CREATE", "USER", savedUser.getUserId(), "Created user: " + savedUser.getEmail());

        log.info("User created: {}", savedUser.getEmail());
        return mapToDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DuplicateResourceException("User already exists with email: " + request.email());
            }
            user.setEmail(request.email());
        }

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.isActive() != null) user.setIsActive(request.isActive());

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            user.setDepartment(department);
        }

        if (request.managerId() != null) {
            User manager = userRepository.findById(request.managerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
            user.setManager(manager);
        }

        if (request.roleIds() != null) {
            Set<Role> roles = request.roleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        auditService.log("UPDATE", "USER", updatedUser.getUserId(), "Updated user: " + updatedUser.getEmail());

        log.info("User updated: {}", updatedUser.getEmail());
        return mapToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);
        auditService.log("DELETE", "USER", userId, "Deleted user: " + user.getEmail());

        log.info("User deleted: {}", user.getEmail());
    }

    @Transactional
    public void deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsActive(false);
        userRepository.save(user);
        auditService.log("DEACTIVATE", "USER", userId, "Deactivated user: " + user.getEmail());

        log.info("User deactivated: {}", user.getEmail());
    }

    @Transactional
    public void activateUser(Long userId) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setIsActive(true);
        userRepository.save(user);
        auditService.log("ACTIVATED", "USER", userId, "activated user: " + user.getEmail());

        log.info("User activated: {}", user.getEmail());
    }

    @Transactional
    public void updateLastLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByDepartment(Long departmentId) {
        log.debug("Fetching users by department: {}", departmentId);
        return userRepository.findByDepartment_DepartmentId(departmentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByManager(Long managerId) {
        log.debug("Fetching users by manager: {}", managerId);
        return userRepository.findByManager_UserId(managerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(String roleName) {
        log.debug("Fetching users by role: {}", roleName);
        return userRepository.findByRoleName(roleName).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null,
                user.getDepartment() != null ? user.getDepartment().getDepartmentName() : null,
                user.getManager() != null ? user.getManager().getUserId() : null,
                user.getManager() != null ? user.getManager().getFullName() : null,
                user.getIsActive(),
                user.getLastLoginAt(),
                user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }
}