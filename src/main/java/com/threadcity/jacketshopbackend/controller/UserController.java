package com.threadcity.jacketshopbackend.controller;

import java.time.Instant;

import com.threadcity.jacketshopbackend.dto.response.ProfileResponse;
import com.threadcity.jacketshopbackend.dto.response.UserReponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.threadcity.jacketshopbackend.dto.request.RegisterRequest;
import com.threadcity.jacketshopbackend.dto.request.UserCreateRequest;
import com.threadcity.jacketshopbackend.dto.request.UserRolesRequset;
import com.threadcity.jacketshopbackend.dto.request.UserStatusRequset;
import com.threadcity.jacketshopbackend.dto.request.UserUpdateRequset;
import com.threadcity.jacketshopbackend.dto.request.ProfileUpdateRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<?> getProfile() {
        log.info("UserController::getProfile execution started");
        ProfileResponse profileResponse = userService.getProfile();
        log.info("UserController::getProfile execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(profileResponse)
                .message("Get profile successfully")
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/me")
    public ApiResponse<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        log.info("UserController::updateProfile execution started");
        ProfileResponse updateProfile = userService.updateProfile(request);
        log.info("UserController::updateProfile execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(updateProfile)
                .message("Update profile successfully")
                .timestamp(Instant.now())
                .build();

    }

    // Admin
    @GetMapping
    public ApiResponse<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sortBy) {
        log.info("UserController::getAllUsers - Execution started");
        PageResponse<?> pageResponse = userService.getAllUsers(page, size, sortBy);
        log.info("UserController::getAllStyles - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get all users successfully.")
                .data(pageResponse)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getUserById(@PathVariable Long id) {
        log.info("UserController::getUserById - Execution started. [id: {}]", id);
        UserReponse response = userService.getUserById(id);
        log.info("UserController::getStyleById - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get user by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("UserController::createUser - Execution started.");
        UserReponse response = userService.createUser(request);
        log.info("UserController::createUser - Execution completed.");
        return ApiResponse.builder()
                .code(201)
                .message("User created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequset requset) {
        log.info("UserController::updateUser - Execution started. [id: {}]", id);
        UserReponse response = userService.updateUserById(requset, id);
        log.info("UserController::updateUser - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("User updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequset requset) {
        log.info("UserController::updateStatus - Execution started. [id: {}]", id);
        UserReponse response = userService.updateUserStatusById(requset, id);
        log.info("UserController::updateStatus - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("User status updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<?> assignRole(@PathVariable Long id, @Valid @RequestBody UserRolesRequset requset) {
        log.info("UserController::assignRole - Execution started. [id: {}]", id);
        UserReponse response = userService.updateUserRolesById(requset, id);
        log.info("UserController::assignRole - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Assigned role for user successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }
}
