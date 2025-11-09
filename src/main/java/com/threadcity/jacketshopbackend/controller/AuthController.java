package com.threadcity.jacketshopbackend.controller;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.threadcity.jacketshopbackend.dto.request.LoginRequest;
import com.threadcity.jacketshopbackend.dto.request.RefreshTokenRequest;
import com.threadcity.jacketshopbackend.dto.request.RegisterRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.LoginResponse;
import com.threadcity.jacketshopbackend.dto.response.TokenResponse;
import com.threadcity.jacketshopbackend.service.TokenService;
import com.threadcity.jacketshopbackend.service.auth.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("AuthController::login execution started");
        LoginResponse loginResponse = authService.login(request);
        log.info("AuthController::login execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(loginResponse)
                .message("Login successfully")
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("AuthController::register execution started");
        authService.register(request);
        log.info("AuthController::register execution ended");
        return ApiResponse.builder()
                .code(201)
                .message("Register successfully")
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("AuthController::logout execution started");
        tokenService.revoke(request.getToken());
        log.info("AuthController::logout execution ended");
        return ApiResponse.builder()
                .code(204)
                .data(null)
                .message("Logout successfully")
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/rotate")
    public ApiResponse<?> rotate(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("AuthController::roate execution started");
        TokenResponse tokenResponse = tokenService.rotate(request.getToken());
        log.info("AuthController::rotate execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(tokenResponse)
                .message("Rotate successfully")
                .timestamp(Instant.now())
                .build();
    }
}
