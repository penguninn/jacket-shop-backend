package com.threadcity.jacketshopbackend.controller;

import java.time.Instant;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.threadcity.jacketshopbackend.dto.request.LoginRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.TokenResponse;
import com.threadcity.jacketshopbackend.service.auth.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authenticationService;

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("AuthenticationController::login execution started");
        TokenResponse tokenResponse = authenticationService.login(request);
        log.info("AuthenticationController::login execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(tokenResponse)
                .message("Login successfully")
                .timestamp(Instant.now())
                .build();
    }

}
