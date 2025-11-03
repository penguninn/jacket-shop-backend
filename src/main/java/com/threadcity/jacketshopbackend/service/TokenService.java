package com.threadcity.jacketshopbackend.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.common.Enums.RefreshTokenStatus;
import com.threadcity.jacketshopbackend.dto.response.TokenResponse;
import com.threadcity.jacketshopbackend.entity.RefreshToken;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.TokenServiceException;
import com.threadcity.jacketshopbackend.mapper.UserMapper;
import com.threadcity.jacketshopbackend.repository.RefreshTokenRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.auth.JwtService;

import jakarta.transaction.Transactional;
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
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + principal.getUsername()));
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
            if (!jwtService.isRefreshToken(refreshToken)
                    || jwtService.isTokenExpired(refreshToken)
                    || !jwtService.isSignatureValid(refreshToken)) {
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
            return;
        }
    }

    @Transactional
    public TokenResponse rotate(String refreshToken) {
        log.info("TokenService::rotate - Execution started");
        if (!jwtService.isSignatureValid(refreshToken)
                || jwtService.isTokenExpired(refreshToken)
                || !jwtService.isRefreshToken(refreshToken)) {
            throw new TokenServiceException("Invalid refresh token");
        }
        String jti = jwtService.extractJti(refreshToken);
        String username = jwtService.extractUsername(refreshToken);

        RefreshToken rf = tokenRepository.findByJti(jti)
                .orElseThrow(() -> new TokenServiceException("Refresh token not recognized"));
        if (rf.getStatus() != RefreshTokenStatus.ACTIVE || rf.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenServiceException("Refresh token not active or expired");
        }

        rf.setStatus(RefreshTokenStatus.REVOKED);

        UserDetails principal = userRepository.findByUsername(username)
                .map(u -> userMapper.toUserDetailsImpl(u))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        log.info("TokenService::rotate - Execution ended");
        return issue(principal);
    }
}
