package com.bmglewis.config;

import com.bmglewis.model.Department;
import com.bmglewis.model.Permission;
import com.bmglewis.model.Role;
import com.bmglewis.model.User;
import com.bmglewis.model.enums.Module;
import com.bmglewis.repository.DepartmentRepository;
import com.bmglewis.repository.PermissionRepository;
import com.bmglewis.repository.RoleRepository;
import com.bmglewis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final DepartmentRepository departmentRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            if (departmentRepository.count() == 0) {
                log.info("Initializing database with sample data...");

                // 1. Create Departments
                initDepartments();

                // 2. Create Permissions
                initPermissions();

                // 3. Create Roles
                initRoles();

                // 4. Create Default Admin User
                initDefaultUser();

                log.info("Database initialization completed!");
            } else {
                log.info("Database already initialized, skipping data load.");
            }
        };
    }

    private void initDepartments() {
        log.info("Creating departments...");

        Department exec = Department.builder()
                .departmentCode("EXEC")
                .departmentName("Executive")
                .isActive(true)
                .build();
        departmentRepository.save(exec);

        List<Department> departments = List.of(
                Department.builder().departmentCode("FIN").departmentName("Finance")
                        .parentDepartment(exec).isActive(true).build(),
                Department.builder().departmentCode("PROC").departmentName("Procurement")
                        .parentDepartment(exec).isActive(true).build(),
                Department.builder().departmentCode("IT").departmentName("Information Technology")
                        .parentDepartment(exec).isActive(true).build(),
                Department.builder().departmentCode("HR").departmentName("Human Resources")
                        .parentDepartment(exec).isActive(true).build(),
                Department.builder().departmentCode("OPS").departmentName("Operations")
                        .parentDepartment(exec).isActive(true).build(),
                Department.builder().departmentCode("SALES").departmentName("Sales & Marketing")
                        .parentDepartment(exec).isActive(true).build()
        );

        departmentRepository.saveAll(departments);
        log.info("Created {} departments", departments.size() + 1);
    }

    private void initPermissions() {
        log.info("Creating permissions...");

        List<Permission> permissions = List.of(
                // User Management
                createPermission(Module.USER_MANAGEMENT, "user.create", "Create new users"),
                createPermission(Module.USER_MANAGEMENT, "user.read", "View user details"),
                createPermission(Module.USER_MANAGEMENT, "user.update", "Update user information"),
                createPermission(Module.USER_MANAGEMENT, "user.delete", "Delete users"),
                createPermission(Module.USER_MANAGEMENT, "role.assign", "Assign roles to users"),

                // Vendor Management
                createPermission(Module.VENDOR_MANAGEMENT, "vendor.create", "Create new vendors"),
                createPermission(Module.VENDOR_MANAGEMENT, "vendor.read", "View vendor details"),
                createPermission(Module.VENDOR_MANAGEMENT, "vendor.update", "Update vendor information"),
                createPermission(Module.VENDOR_MANAGEMENT, "vendor.delete", "Delete vendors"),
                createPermission(Module.VENDOR_MANAGEMENT, "vendor.approve", "Approve vendor registration"),

                // Purchase Requisition
                createPermission(Module.PURCHASE_REQUISITION, "pr.create", "Create purchase requisitions"),
                createPermission(Module.PURCHASE_REQUISITION, "pr.read", "View purchase requisitions"),
                createPermission(Module.PURCHASE_REQUISITION, "pr.update", "Update purchase requisitions"),
                createPermission(Module.PURCHASE_REQUISITION, "pr.delete", "Delete purchase requisitions"),
                createPermission(Module.PURCHASE_REQUISITION, "pr.approve", "Approve purchase requisitions"),
                createPermission(Module.PURCHASE_REQUISITION, "pr.reject", "Reject purchase requisitions"),

                // Settings
                createPermission(Module.SETTINGS, "settings.view", "View system settings"),
                createPermission(Module.SETTINGS, "settings.update", "Update system settings")
        );

        permissionRepository.saveAll(permissions);
        log.info("Created {} permissions", permissions.size());
    }

    private Permission createPermission(Module module, String name, String description) {
        return Permission.builder()
                .module(module)
                .permissionName(name)
                .description(description)
                .build();
    }

    private void initRoles() {
        log.info("Creating roles...");

        // Get all permissions
        List<Permission> allPermissions = permissionRepository.findAll();

        // System Admin - all permissions
        Role adminRole = Role.builder()
                .roleName("System Admin")
                .description("Full system access and configuration")
                .isActive(true)
                .permissions(new HashSet<>(allPermissions))
                .build();
        roleRepository.save(adminRole);

        // Requester - limited permissions
        Set<Permission> requesterPerms = new HashSet<>();
        requesterPerms.addAll(permissionRepository.findByModule(Module.PURCHASE_REQUISITION));
        requesterPerms.add(permissionRepository.findByPermissionName("vendor.read").orElse(null));

        Role requesterRole = Role.builder()
                .roleName("Requester")
                .description("Create purchase requisitions")
                .isActive(true)
                .permissions(requesterPerms)
                .build();
        roleRepository.save(requesterRole);

        log.info("Created roles");
    }

    private void initDefaultUser() {
        log.info("Creating default admin user...");

        Role adminRole = roleRepository.findByRoleName("System Admin").orElseThrow();
        Department itDept = departmentRepository.findByDepartmentCode("IT").orElseThrow();

        User admin = User.builder()
                .email("admin@bmglewis.com")
                .password(passwordEncoder.encode("Admin123!"))
                .firstName("System")
                .lastName("Administrator")
                .phone("+1234567890")
                .department(itDept)
                .isActive(true)
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();

        userRepository.save(admin);
        log.info("Created admin user: admin@bmglewis.com / Admin123!");
    }
}