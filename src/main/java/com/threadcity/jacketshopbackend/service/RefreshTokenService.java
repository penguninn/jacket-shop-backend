package com.threadcity.jacketshopbackend.service;

import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.repository.RefreshTokenRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.auth.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository tokenRepository;
    private final UserRepository userRepository;
}
