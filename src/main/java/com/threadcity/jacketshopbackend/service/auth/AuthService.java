package com.threadcity.jacketshopbackend.service.auth;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.LoginRequest;
import com.threadcity.jacketshopbackend.dto.request.RegisterRequest;
import com.threadcity.jacketshopbackend.dto.response.LoginResponse;
import com.threadcity.jacketshopbackend.dto.response.TokenResponse;
import com.threadcity.jacketshopbackend.dto.response.UserResponse;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.AuthenticationFailedException;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.RoleMapper;
import com.threadcity.jacketshopbackend.repository.RoleRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final TokenService tokenService;
        private final RoleMapper roleMapper;

        @Transactional
        public LoginResponse login(LoginRequest request) {
                log.info("AuthService::login execution started");
                // We fetch the user first to build the response later,
                // but we don't want to reveal if the user exists or not if auth fails.
                // However, standard flow often typically separates these.
                // For security, if not found, we effectively will fail at authenticationManager
                // step usually.
                // But here we rely on this user object.
                // We will throw same exception as BadCredentials to mask it.
                User user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new AuthenticationFailedException(
                                                ErrorCodes.AUTH_INVALID_CREDENTIALS,
                                                "Invalid username or password"));

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
                                        .user(UserResponse.builder()
                                                        .id(user.getId())
                                                        .username(user.getUsername())
                                                        .fullName(user.getFullName())
                                                        .phone(user.getPhone())
                                                        .status(user.getStatus())
                                                        .roles(user.getRoles().stream().map(roleMapper::toDto).toList())
                                                        .build())
                                        .build();
                        log.info("AuthService::login execution ended");
                        return loginResponse;
                } catch (AuthenticationException e) {
                        log.warn("Authentication failed for user: {}", request.getUsername());
                        throw new AuthenticationFailedException(ErrorCodes.AUTH_INVALID_CREDENTIALS,
                                        "Invalid username or password",
                                        e);
                }
        }

        @Transactional
        public void register(RegisterRequest request) {
                log.info("AuthService::register execution started");
                if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                        throw new ResourceConflictException(ErrorCodes.USER_USERNAME_DUPLICATE,
                                        "Username already exists");
                }
                Role role = roleRepository.findByName("CUSTOMER")
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ROLE_NOT_FOUND,
                                                "Default role CUSTOMER not found"));

                User user = User.builder()
                                .username(request.getUsername())
                                .fullName(request.getFullName())
                                .phone(request.getPhoneNumber())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .status(Status.ACTIVE)
                                .build();
                user.getRoles().add(role);
                user = userRepository.save(user);

                // Ensure user is loaded in cache or context if needed, though mostly used for
                // session/token which we don't generate here?
                // The original code called loadUserByUsername, but didn't use the result.
                // userDetailsService.loadUserByUsername(user.getUsername());

                log.info("AuthService::register execution ended");
        }

}
