package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.RoleResponse;
import com.threadcity.jacketshopbackend.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ApiResponse<?> getAllRoles() {
        log.info("RoleCotroller::getAllRoles execution started");
        List<RoleResponse> roleResponses = roleService.getAllRoles();
        log.info("RoleCotroller::getAllRoles execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(roleResponses)
                .message("Get all roles successfully")
                .timestamp(Instant.now())
                .build();
    }
}
