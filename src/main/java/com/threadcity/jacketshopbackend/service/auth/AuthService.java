package com.threadcity.jacketshopbackend.service.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.dto.request.LoginRequest;
import com.threadcity.jacketshopbackend.dto.response.TokenResponse;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.AuthServiceException;
import com.threadcity.jacketshopbackend.repository.RefreshTokenRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.RefreshTokenService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService tokenService;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("AuthService::login execution started");
        userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails principal = (UserDetailsImpl) authentication.getPrincipal();
            TokenResponse tokenResponse = tokenService.issue(principal);
            log.info("AuthService::authenticate execution ended");
            return tokenResponse;
        } catch (BadCredentialsException e) {
            log.error("Bad credentials: {}", e.getMessage());
            throw new AuthServiceException("Invalid username or password");
        } catch (RuntimeException e) {
            log.error("Exception occurred while authenticate, Exception message: {}", e.getMessage());
            throw new AuthServiceException("authenticate failed");
        }
    }


}
