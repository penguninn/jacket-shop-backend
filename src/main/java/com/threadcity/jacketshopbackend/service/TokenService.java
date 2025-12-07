package com.threadcity.jacketshopbackend.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.threadcity.jacketshopbackend.common.Enums.RefreshTokenStatus;
import com.threadcity.jacketshopbackend.dto.response.TokenResponse;
import com.threadcity.jacketshopbackend.entity.RefreshToken;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.AuthenticationFailedException;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.UserMapper;
import com.threadcity.jacketshopbackend.repository.RefreshTokenRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.auth.JwtService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${jwt.refresh-ttl-ms:604800000}")
    private long refreshTokenExpiration;

    @Transactional
    public TokenResponse issue(UserDetails principal) {
        log.info("TokenService::issue - Execution started");
        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.USER_NOT_FOUND, "User not found with username: " + principal.getUsername()));
        String jti = UUID.randomUUID().toString();
        String access = jwtService.generateAccessToken(principal, jti);
        String refresh = jwtService.generateRefreshToken(principal, jti);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .jti(jti)
                .status(RefreshTokenStatus.ACTIVE)
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpiration))
                .build();
        tokenRepository.save(refreshToken);
        log.info("TokenService::issue - Execution ended");
        return TokenResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }

    @Transactional
    public void revoke(String refreshToken) {
        try {
            log.info("TokenService::revoke - Execution started");
            if (!jwtService.isSignatureValid(refreshToken)
                    || jwtService.isTokenExpired(refreshToken)
                    || !jwtService.isRefreshToken(refreshToken)) { // Note: isTokenExpired might throw/return logic
                return;
            }
            String jti = jwtService.extractJti(refreshToken);
            if (jti == null) {
                return;
            }
            tokenRepository.findByJti(jti).ifPresent(rt -> {
                if (rt.getStatus() != RefreshTokenStatus.REVOKED) {
                    rt.setStatus(RefreshTokenStatus.REVOKED);
                }
            });
            log.info("TokenService::revoke - Execution ended");
        } catch (Exception e) {
            log.warn("Token revocation failed specifically: {}", e.getMessage());
            // Swallowing exception as revocation failure shouldn't crash the logout flow
            // usually
        }
    }

    @Transactional
    public TokenResponse rotate(String refreshToken) {
        log.info("TokenService::rotate - Execution started");

        // We can split checks for better error messages
        if (!jwtService.isSignatureValid(refreshToken)) {
            throw new AuthenticationFailedException(ErrorCodes.AUTH_REFRESH_TOKEN_INVALID,
                    "Invalid refresh token signature");
        }
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new AuthenticationFailedException(ErrorCodes.AUTH_TOKEN_EXPIRED, "Refresh token expired");
        }
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new AuthenticationFailedException(ErrorCodes.AUTH_REFRESH_TOKEN_INVALID, "Not a refresh token");
        }

        String jti = jwtService.extractJti(refreshToken);
        String username = jwtService.extractUsername(refreshToken);

        RefreshToken rf = tokenRepository.findByJti(jti)
                .orElseThrow(() -> new AuthenticationFailedException(ErrorCodes.AUTH_REFRESH_TOKEN_INVALID,
                        "Refresh token not found"));

        if (rf.getStatus() != RefreshTokenStatus.ACTIVE) {
            throw new AuthenticationFailedException(ErrorCodes.AUTH_REFRESH_TOKEN_INVALID, "Refresh token is revoked");
        }
        if (rf.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthenticationFailedException(ErrorCodes.AUTH_TOKEN_EXPIRED, "Refresh token expired");
        }

        rf.setStatus(RefreshTokenStatus.REVOKED);

        UserDetails principal = userRepository.findByUsername(username)
                .map(u -> userMapper.toUserDetailsImpl(u))
                .orElseThrow(
                        () -> new AuthenticationFailedException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User not found"));

        log.info("TokenService::rotate - Execution ended");
        return issue(principal);
    }
}
