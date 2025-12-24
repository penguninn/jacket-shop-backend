package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.AddressRequest;
import com.threadcity.jacketshopbackend.dto.response.AddressResponse;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/user-addresses")
@RequiredArgsConstructor
@Slf4j
public class UserAddressController {

    private final AddressService addressService;

    @GetMapping
    public ApiResponse<?> getAllAddressByUserId() {
        log.info("UserAddressController::getAllAddressByUserId - Execution started");
        List<AddressResponse> response = addressService.getAllAddressByUserId();
        log.info("UserAddressController::getAllAddressByUserId - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get all addresses by user id successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<?> getAddressesByUserId(@PathVariable Long userId) {
        log.info("UserAddressController::getAddressesByUserId - Execution started. [userId: {}]", userId);
        List<AddressResponse> response = addressService.getAddressesByUserId(userId);
        log.info("UserAddressController::getAddressesByUserId - Execution completed. [userId: {}]", userId);
        return ApiResponse.builder()
                .code(200)
                .message("Get all addresses by user id successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/default")
    public ApiResponse<?> getDefaultAddress() {
        log.info("UserAddressController::getDefaultAddress - Execution started");
        AddressResponse response = addressService.getDefaultAddress();
        log.info("UserAddressController::getDefaultAddress - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get default address successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/user/{userId}")
    public ApiResponse<?> createAddressForUser(@PathVariable Long userId, @Valid @RequestBody AddressRequest request) {
        log.info("UserAddressController::createAddressForUser - Execution started. [userId: {}]", userId);
        AddressResponse response = addressService.createAddress(userId, request);
        log.info("UserAddressController::createAddressForUser - Execution completed. [userId: {}]", userId);
        return ApiResponse.builder()
                .code(201)
                .message("Create address successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/user/{userId}/{addressId}")
    public ApiResponse<?> updateAddressForUser(@PathVariable Long userId, @PathVariable Long addressId, @Valid @RequestBody AddressRequest request) {
        log.info("UserAddressController::updateAddressForUser - Execution started. [userId: {}, addressId: {}]", userId, addressId);
        AddressResponse response = addressService.updateAddress(userId, addressId, request);
        log.info("UserAddressController::updateAddressForUser - Execution completed. [userId: {}, addressId: {}]", userId, addressId);
        return ApiResponse.builder()
                .code(200)
                .message("Update address successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createAddress(@Valid @RequestBody AddressRequest request) {
        log.info("UserAddressController::createAddress - Execution started");
        AddressResponse response = addressService.createAddress(request);
        log.info("UserAddressController::createAddress - Execution completed");
        return ApiResponse.builder()
                .code(201)
                .message("Create address successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        log.info("UserAddressController::updateAddress - Execution started. [id: {}]", id);
        AddressResponse response = addressService.updateAddress(request, id);
        log.info("UserAddressController::updateAddress - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Update address successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/default")
    public ApiResponse<?> setDefaultAddress(@PathVariable Long id) {
        log.info("UserAddressController::setDefaultAddress - Execution started. [id: {}]", id);
        AddressResponse response = addressService.setDefaultAddress(id);
        log.info("UserAddressController::setDefaultAddress - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Set default address successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteAddress(@PathVariable Long id) {
        log.info("UserAddressController::deleteAddress - Execution started. [id: {}]", id);
        addressService.deleteAddress(id);
        log.info("UserAddressController::deleteAddress - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Delete address successfully.")
                .timestamp(Instant.now())
                .build();
    }
}
