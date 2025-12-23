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

    private final LocationService locationService;
    private final ProvinceRepository provinceRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initDataOnStartup() {
        if (provinceRepository.count() == 0) {
            locationService.syncAllAddressData();
        }
        initAdminUser();
        initPaymentMethods();
    }

    private void initPaymentMethods() {
        createPaymentMethodIfNotFound("Thanh toán khi nhận hàng", "COD", Enums.PaymentMethodType.ONLINE, null);
        createPaymentMethodIfNotFound("Thẻ quốc tế (Visa/Master)", "STRIPE", Enums.PaymentMethodType.ONLINE, "{\"publicKey\": \"pk_test_...\"}");
        createPaymentMethodIfNotFound("Chuyển khoản (VietQR)", "VIETQR_ONLINE", Enums.PaymentMethodType.ONLINE, "{\"bankId\": \"MB\", \"acc\": \"...\"}");
        createPaymentMethodIfNotFound("Tiền mặt", "CASH", Enums.PaymentMethodType.POS, null);
        createPaymentMethodIfNotFound("Chuyển khoản tại quầy", "VIETQR_POS", Enums.PaymentMethodType.POS, "{\"bankId\": \"MB\", \"acc\": \"...\"}");
    }

    private void createPaymentMethodIfNotFound(String name, String code, Enums.PaymentMethodType type, String config) {
        if (!paymentMethodRepository.existsByCode(code)) {
            PaymentMethod method = PaymentMethod.builder()
                    .name(name)
                    .code(code)
                    .type(type)
                    .config(config)
                    .status(Enums.Status.ACTIVE)
                    .build();
            paymentMethodRepository.save(method);
            log.info("Payment method {} created.", code);
        }
    }

    private void initAdminUser() {
        createRoleIfNotFound("ADMIN", "Administrator role");
        createRoleIfNotFound("STAFF", "Staff role");
        createRoleIfNotFound("CUSTOMER", "Customer role");

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
            log.info("Admin user created successfully.");
        }
    }

    private void createRoleIfNotFound(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = Role.builder()
                    .name(name)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Role {} created.", name);
        }
    }
}
