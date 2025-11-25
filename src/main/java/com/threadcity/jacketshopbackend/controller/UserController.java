package com.threadcity.jacketshopbackend.controller;

import java.time.Instant;
import java.util.List;

import com.threadcity.jacketshopbackend.dto.response.ProfileResponse;
import com.threadcity.jacketshopbackend.dto.response.UserReponse;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.threadcity.jacketshopbackend.dto.request.RegisterRequest;
import com.threadcity.jacketshopbackend.dto.request.UserBulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.UserBulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.UserCreateRequest;
import com.threadcity.jacketshopbackend.dto.request.UserFilterRequest;
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
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) List<String> roles,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        log.info("UserController::getAllUsers - Execution started");
        UserFilterRequest request = UserFilterRequest.builder()
                .search(search)
                .status(status)
                .roles(roles)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();
        PageResponse<?> pageResponse = userService.getAllUsers(request);
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

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Long id) {
        log.info("UserController::deleteUser - Execution started. [id: {}]", id);
        userService.deleteUserById(id);
        log.info("UserController::deleteUser - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("User deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/bulk/status")
    public ApiResponse<?> updateUsersStatus(@Valid @RequestBody UserBulkStatusRequest request) {
        int totalUserIds = request.getIds() != null ? request.getIds().size() : 0;
        log.info("UserController::updateUsersStatus - Execution started. [totalIds: {}]", totalUserIds);
        userService.updateUsersStatusBulk(request);
        log.info("UserController::updateUsersStatus - Execution completed. [totalIds: {}]", totalUserIds);
        return ApiResponse.builder()
                .code(200)
                .message("User statuses updated successfully.")
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/bulk/delete")
    public ApiResponse<?> deleteUsersBulk(@Valid @RequestBody UserBulkDeleteRequest request) {
        int totalUserIds = request.getIds() != null ? request.getIds().size() : 0;
        log.info("UserController::deleteUsersBulk - Execution started. [totalIds: {}]", totalUserIds);
        userService.deleteUsersBulk(request);
        log.info("UserController::deleteUsersBulk - Execution completed. [totalIds: {}]", totalUserIds);
        return ApiResponse.builder()
                .code(200)
                .message("Users deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }
}
