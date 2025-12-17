package com.threadcity.jacketshopbackend.configuration;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.repository.ProvinceRepository;
import com.threadcity.jacketshopbackend.repository.RoleRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.AddressSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSyncConfig {

    private final AddressSyncService addressSyncService;
    private final ProvinceRepository provinceRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initDataOnStartup() {
        if (provinceRepository.count() == 0) {
            log.info("Database empty. Starting initial address sync...");
            addressSyncService.syncAllAddressData();
        }
        initAdminUser();
    }

    private void initAdminUser() {
        // Create Roles
        createRoleIfNotFound("ADMIN", "Administrator role");
        createRoleIfNotFound("STAFF", "Staff role");
        createRoleIfNotFound("CUSTOMER", "Customer role");

        // Create Admin User
        if (userRepository.findByUsername("admin").isEmpty()) {
            log.info("Admin user not found. Creating...");

            Set<Role> roles = new HashSet<>();
            roleRepository.findByName("ADMIN").ifPresent(roles::add);
            roleRepository.findByName("STAFF").ifPresent(roles::add);
            roleRepository.findByName("CUSTOMER").ifPresent(roles::add);

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin")) // Default password
                    .fullName("Administrator")
                    .phone("0000000000")
                    .status(Enums.Status.ACTIVE)
                    .roles(roles)
                    .build();

            userRepository.save(admin);
            log.info("Admin user created successfully.");
        }
    }

    private void createRoleIfNotFound(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .status(Enums.Status.ACTIVE)
                    .build();
            roleRepository.save(role);
            log.info("Role {} created.", name);
        }
    }
}
