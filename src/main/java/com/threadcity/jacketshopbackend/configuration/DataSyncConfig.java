package com.threadcity.jacketshopbackend.configuration;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.entity.PaymentMethod;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.repository.PaymentMethodRepository;
import com.threadcity.jacketshopbackend.repository.ProvinceRepository;
import com.threadcity.jacketshopbackend.repository.RoleRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSyncConfig {

    private final LocationService locationService;
    private final ProvinceRepository provinceRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Lazy
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initDataOnStartup() {
        log.info("Starting data initialization...");
        initLocations();
        initRoles();
        initUsers();
        initPaymentMethods();
        log.info("Data initialization completed.");
    }

    private void initLocations() {
        if (provinceRepository.count() == 0) {
            locationService.syncAllAddressData();
        }
    }

    private void initRoles() {
        createRoleIfNotFound("ADMIN", "Administrator role");
        createRoleIfNotFound("STAFF", "Staff role");
        createRoleIfNotFound("CUSTOMER", "Customer role");
    }

    private void initUsers() {
        initAdminUser();
        initGuestUser();
    }

    private void initPaymentMethods() {
        createPaymentMethodIfNotFound("Thanh toán khi nhận hàng", "COD", Enums.PaymentMethodType.ONLINE, null);
        createPaymentMethodIfNotFound("Chuyển khoản (QR)", "QR", Enums.PaymentMethodType.ONLINE, "{\"bankId\": \"MB\", \"acc\": \"...\"}");
        createPaymentMethodIfNotFound("Chuyển khoản (QR)", "QR_INSTORE", Enums.PaymentMethodType.POS, "{\"bankId\": \"MB\", \"acc\": \"...\"}");
        createPaymentMethodIfNotFound("Tiền mặt", "CASH", Enums.PaymentMethodType.POS, null);
    }

    private void initAdminUser() {
        if (userRepository.findByUsername("admin123").isEmpty()) {
            log.info("Admin user not found. Creating...");
            Set<Role> roles = new HashSet<>();
            roleRepository.findByName("ADMIN").ifPresent(roles::add);
            roleRepository.findByName("STAFF").ifPresent(roles::add);
            roleRepository.findByName("CUSTOMER").ifPresent(roles::add);

            User admin = User.builder()
                    .username("admin123")
                    .password(passwordEncoder.encode("12345678"))
                    .fullName("Administrator")
                    .phone("0987654321")
                    .status(Enums.Status.ACTIVE)
                    .roles(roles)
                    .build();

            userRepository.save(admin);
        }
    }

    private void initGuestUser() {
        if (userRepository.findByUsername("guest").isEmpty()) {
            log.info("Guest user (Walk-in Customer) not found. Creating...");
            Set<Role> roles = new HashSet<>();
            roleRepository.findByName("CUSTOMER").ifPresent(roles::add);

            User guest = User.builder()
                    .username("guest")
                    .password(passwordEncoder.encode("guest@123"))
                    .fullName("Khách lẻ")
                    .phone("0000000000")
                    .status(Enums.Status.ACTIVE)
                    .roles(roles)
                    .build();

            userRepository.save(guest);
        }
    }

    private void createRoleIfNotFound(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();
            roleRepository.save(role);
        }
    }

    private void createPaymentMethodIfNotFound(String name, String code, Enums.PaymentMethodType type, String config) {
        if (!paymentMethodRepository.existsByCode(code)) {
            PaymentMethod method = PaymentMethod.builder()
                    .name(name)
                    .code(code)
                    .type(type)
                    .status(Enums.Status.ACTIVE)
                    .build();
            paymentMethodRepository.save(method);
        }
    }
}
