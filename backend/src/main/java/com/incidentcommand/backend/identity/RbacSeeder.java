package com.incidentcommand.backend.identity;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the fixed global roles/permissions from docs/15-security-and-rbac.md.
 * Idempotent -- safe to run on every startup. Real role/permission editing is
 * an ADMIN capability to build later (docs/16-ui-product-and-design-spec.md);
 * for now this is the only way roles/permissions get created.
 */
@Component
public class RbacSeeder implements CommandLineRunner {

    private static final List<String> PERMISSIONS = List.of(
            "incidents.read", "incidents.investigate", "incidents.approve",
            "production.read", "production.manage",
            "teams.manage", "users.manage", "roles.manage",
            "sla_policy.manage", "audit.read", "analytics.read");

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RbacSeeder(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        PERMISSIONS.forEach(name -> permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(new Permission(name))));

        List.of("USER", "OPERATOR", "ADMIN")
                .forEach(name -> roleRepository.findByName(name)
                        .orElseGet(() -> roleRepository.save(new Role(name))));
    }
}
