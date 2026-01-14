package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.auth.request.*;
import com.threadcity.jacketshopbackend.dto.auth.response.LoginResponse;
import com.threadcity.jacketshopbackend.dto.auth.response.TokenResponse;
import com.threadcity.jacketshopbackend.dto.common.response.ApiResponse;
import com.threadcity.jacketshopbackend.service.TokenService;
import com.threadcity.jacketshopbackend.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

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

    @PostMapping("/refresh")
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

    @PostMapping("/forgot-password")
    public ApiResponse<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("AuthController::forgotPassword execution started");
        authService.forgotPassword(request);
        log.info("AuthController::forgotPassword execution ended");
        return ApiResponse.builder()
                .code(200)
                .message("If the account exists, a password reset email has been sent")
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/verify-reset-token")
    public ApiResponse<?> verifyResetToken(@RequestParam String token) {
        log.info("AuthController::verifyResetToken execution started");
        boolean isValid = authService.verifyResetToken(token);
        log.info("AuthController::verifyResetToken execution ended");
        return ApiResponse.builder()
                .code(200)
                .message(isValid ? "Token is valid" : "Token is invalid or expired")
                .data(isValid)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("AuthController::resetPassword execution started");
        authService.resetPassword(request);
        log.info("AuthController::resetPassword execution ended");
        return ApiResponse.builder()
                .code(200)
                .message("Password has been reset successfully")
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/update-password")
    public ApiResponse<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        log.info("AuthController::updatePassword execution started");
        authService.updatePassword(request);
        log.info("AuthController::updatePassword execution ended");
        return ApiResponse.builder()
                .code(200)
                .message("Password updated successfully")
                .timestamp(Instant.now())
                .build();
    }
}
