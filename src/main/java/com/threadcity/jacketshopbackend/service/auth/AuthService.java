package com.threadcity.jacketshopbackend.service.auth;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.auth.request.ForgotPasswordRequest;
import com.threadcity.jacketshopbackend.dto.auth.request.LoginRequest;
import com.threadcity.jacketshopbackend.dto.auth.request.RegisterRequest;
import com.threadcity.jacketshopbackend.dto.auth.request.ResetPasswordRequest;
import com.threadcity.jacketshopbackend.dto.auth.request.UpdatePasswordRequest;
import com.threadcity.jacketshopbackend.dto.auth.response.LoginResponse;
import com.threadcity.jacketshopbackend.dto.auth.response.TokenResponse;
import com.threadcity.jacketshopbackend.dto.user.response.UserResponse;
import com.threadcity.jacketshopbackend.entity.PasswordResetToken;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.AuthenticationFailedException;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.mapper.RoleMapper;
import com.threadcity.jacketshopbackend.repository.PasswordResetTokenRepository;
import com.threadcity.jacketshopbackend.repository.RoleRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.EmailService;
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

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

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
        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final EmailService emailService;

        @Transactional
        public LoginResponse login(LoginRequest request) {
                log.info("AuthService::login execution started");
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
                                                        .roles(user.getRoles().stream().map(roleMapper::toResponse).toList())
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
                            .email(request.getEmail())
                                .phone(request.getPhone())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .status(Status.ACTIVE)
                                .build();
                user.getRoles().add(role);
                userRepository.save(user);
                log.info("AuthService::register execution ended");
        }

        @Transactional
        public void forgotPassword(ForgotPasswordRequest request) {
                log.info("AuthService::forgotPassword execution started for user: {}", request.getUsername());
                User user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND,
                                                "User not found"));

                // Check if user has email
                if (user.getEmail() == null || user.getEmail().isBlank()) {
                        throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED,
                                        "User does not have an email address configured");
                }

                // Delete old reset tokens for this user
                passwordResetTokenRepository.deleteByUser(user);

                // Create new token
                String token = UUID.randomUUID().toString();
                PasswordResetToken resetToken = PasswordResetToken.builder()
                                .token(token)
                                .user(user)
                                .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                                .isUsed(false)
                                .build();

                passwordResetTokenRepository.save(resetToken);

                // Send email
                try {
                        emailService.sendPasswordResetEmail(user.getEmail(), token);
                        log.info("AuthService::forgotPassword - Reset email sent to user: {}", user.getUsername());
                } catch (IOException e) {
                        log.error("AuthService::forgotPassword - Failed to send reset email: {}", e.getMessage());
                        throw new InvalidRequestException(ErrorCodes.AUTH_EMAIL_SEND_FAILED,
                                        "Failed to send password reset email. Please try again later.");
                }

                log.info("AuthService::forgotPassword execution ended");
        }


        public boolean verifyResetToken(String token) {
                log.info("AuthService::verifyResetToken execution started");
                PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                                .orElse(null);

                if (resetToken == null) {
                        log.warn("AuthService::verifyResetToken - Token not found");
                        return false;
                }

                if (resetToken.getIsUsed()) {
                        log.warn("AuthService::verifyResetToken - Token already used");
                        return false;
                }

                if (resetToken.getExpiryDate().isBefore(Instant.now())) {
                        log.warn("AuthService::verifyResetToken - Token expired");
                        return false;
                }
                log.info("AuthService::verifyResetToken - Token is valid");
                return true;
        }

        @Transactional
        public void resetPassword(ResetPasswordRequest request) {
                log.info("AuthService::resetPassword execution started");

                PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                                .orElseThrow(() -> new AuthenticationFailedException(
                                                ErrorCodes.AUTH_RESET_TOKEN_INVALID,
                                                "Invalid or expired reset token"));

                if (resetToken.getIsUsed()) {
                        throw new AuthenticationFailedException(
                                        ErrorCodes.AUTH_RESET_TOKEN_USED,
                                        "This reset token has already been used");
                }

                if (resetToken.getExpiryDate().isBefore(Instant.now())) {
                        throw new AuthenticationFailedException(
                                        ErrorCodes.AUTH_RESET_TOKEN_EXPIRED,
                                        "Reset token has expired. Please request a new one.");
                }

                User user = resetToken.getUser();
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                resetToken.setIsUsed(true);
                passwordResetTokenRepository.save(resetToken);
                log.info("AuthService::resetPassword - Password reset successfully for user: {}", user.getUsername());
        }

        @Transactional
        public void updatePassword(UpdatePasswordRequest request) {
                log.info("AuthService::updatePassword execution started");
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                User user = userRepository.findById(userDetails.getId())
                                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND,
                                                "User not found"));

                if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                        throw new AuthenticationFailedException(ErrorCodes.AUTH_INVALID_CREDENTIALS,
                                        "Old password does not match");
                }

                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);
                log.info("AuthService::updatePassword execution ended");
        }

}
