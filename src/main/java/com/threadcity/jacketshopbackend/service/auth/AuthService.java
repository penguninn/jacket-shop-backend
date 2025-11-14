package com.threadcity.jacketshopbackend.service.auth;

import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.LoginRequest;
import com.threadcity.jacketshopbackend.dto.request.RegisterRequest;
import com.threadcity.jacketshopbackend.dto.response.LoginResponse;
import com.threadcity.jacketshopbackend.dto.response.TokenResponse;
import com.threadcity.jacketshopbackend.dto.response.ProfileResponse;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.AuthServiceException;
import com.threadcity.jacketshopbackend.exception.UsernameAlreadyExistsException;
import com.threadcity.jacketshopbackend.repository.RoleRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.TokenService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("AuthService::login execution started");
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails principal = (UserDetailsImpl) authentication.getPrincipal();
            TokenResponse tokenResponse = tokenService.issue(principal);
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .user(ProfileResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .fullName(user.getFullName())
                            .phone(user.getPhone())
                            .roles(user.getRoles().stream()
                                    .map(Role::getName)
                                    .collect(Collectors.toSet()))
                            .build())
                    .build();
            log.info("AuthService::login execution ended");
            return loginResponse;
        } catch (BadCredentialsException e) {
            log.error("Bad credentials: {}", e.getMessage());
            throw new AuthServiceException("Invalid username or password");
        } catch (RuntimeException e) {
            log.error("Exception occurred while authenticate, Exception message: {}", e.getMessage());
            throw new AuthServiceException("Login failed");
        }
    }

    @Transactional
    public void register(RegisterRequest request) {
        log.info("AuthService::register execution started");
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        Role role = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        try {
            User user = User.builder()
                    .username(request.getUsername())
                    .fullName(request.getFullName())
                    .phone(request.getPhoneNumber())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .status(Status.ACTIVE)
                    .build();
            user.getRoles().add(role);
            user = userRepository.save(user);
            UserDetails principal = userDetailsService.loadUserByUsername(user.getUsername());
            log.info("AuthService::register execution ended");
        } catch (RuntimeException e) {
            log.error("Exception occurred while register, Exception message: {}", e.getMessage());
            throw new AuthServiceException("register failed");
        }
    }

}
