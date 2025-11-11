package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProfileResponse;
import com.threadcity.jacketshopbackend.dto.response.UserReponse;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.ProfileUpdateRequest;
import com.threadcity.jacketshopbackend.dto.request.UserCreateRequest;
import com.threadcity.jacketshopbackend.dto.request.UserRolesRequset;
import com.threadcity.jacketshopbackend.dto.request.UserStatusRequset;
import com.threadcity.jacketshopbackend.dto.request.UserUpdateRequset;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.UserMapper;
import com.threadcity.jacketshopbackend.repository.RoleRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile() {
        log.info("UserService::getProfile execution started");
        Long userId = getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        log.info("UserService::getProfile execution ended");
        return userMapper.toProfile(user);
    }

    public ProfileResponse updateProfile(ProfileUpdateRequest request) {
        log.info("UserService::updateProfile execution started");
        Long userId = getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setFullName(request.getFullName());
        userRepository.save(user);
        log.info("UserService::updateProfile execution ended");
        return userMapper.toProfile(user);
    }

    public UserReponse getUserById(Long Id) {
        log.info("UserService::getUserById - Execution started. [Id: {}]", Id);
        User user = userRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with UserId: " + Id));
        log.info("UserService::getUserById - Execution completed. [UserId: {}]", Id);
        return userMapper.toUserReponse(user);
    }

    public PageResponse<?> getAllUsers(int page, int size, String sortBy) {
        log.info("UserService::getAllUsers - Execution started.");
        try {
            int p = Math.max(0, page);
            String[] sortParams = sortBy.split(",");
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<User> userPage = userRepository.findAll(pageable);
            List<UserReponse> usersResponse = userPage.stream()
                    .map(userMapper::toUserReponse)
                    .toList();
            log.info("UserService::getAllUsers - Execution completed.");
            return PageResponse.builder()
                    .contents(usersResponse)
                    .size(size)
                    .page(p)
                    .totalPages(userPage.getTotalPages())
                    .totalElements(userPage.getTotalElements()).build();
        } catch (Exception e) {
            log.error("UserService::getAllUsers - Execution failed.", e);
            throw new BusinessException("UserService::getAllUsers - Execution failed.");
        }
    }

    @Transactional
    public UserReponse createUser(UserCreateRequest request) {
        log.info("UserService::createUser - Execution started.");
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("User already exists with username: " + request.getUsername());
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
            log.info("UserService::createUser - Execution completed.");
            return userMapper.toUserReponse(user);
        } catch (Exception e) {
            log.error("UserService::createUser - Execution failed.", e);
            throw new BusinessException("UserService::createUser - Execution failed.");
        }
    }

    @Transactional
    public UserReponse updateUserById(UserUpdateRequset request, Long id) {
        log.info("UserService::updateUserByid - Execution started.");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with userId: " + id));
        try {
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhoneNumber());
            User userSaved = userRepository.save(user);
            log.info("UserService::updateProfile - Execution completed. [UserId: {}]", id);
            return userMapper.toUserReponse(userSaved);
        } catch (RuntimeException e) {
            log.error("UserService::updateProfile - Execution failed.", e);
            throw new BusinessException("UserService::updateProfile - Execution failed.");
        }
    }

    @Transactional
    public UserReponse updateUserStatusById(UserStatusRequset request, Long id) {
        log.info("UserService::updateUserStatusById - Execution started.");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with userId: " + id));
        try {
            user.setStatus(request.getStatus());
            User userSaved = userRepository.save(user);
            log.info("UserService::updateUserStatusById - Execution completed. [UserId: {}]", id);
            return userMapper.toUserReponse(userSaved);
        } catch (RuntimeException e) {
            log.error("UserService::updateUserStatusById - Execution failed.", e);
            throw new BusinessException("UserService::updateUserStatusById - Execution failed.");
        }
    }

    @Transactional
    public UserReponse updateUserRolesById(UserRolesRequset request, Long id) {
        log.info("UserService::updateUserRolesById - Execution started.");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with userId: " + id));
        Set<Role> roles = roleRepository.findAllById(request.getRoleIds()).stream().collect(Collectors.toSet());
        if (roles.size() != request.getRoleIds().size()) {
            Set<Long> foundIds = roles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());
            Set<Long> notFoundIds = new HashSet<>(request.getRoleIds());
            notFoundIds.removeAll(foundIds);
            throw new EntityNotFoundException("Roles not found: " + notFoundIds);
        }
        try {
            user.setRoles(roles);
            User userSaved = userRepository.save(user);
            log.info("UserService::updateUserRolesById - Execution completed. [UserId: {}]", id);
            return userMapper.toUserReponse(userSaved);
        } catch (RuntimeException e) {
            log.error("UserService::updateUserRolesById - Execution failed.", e);
            throw new BusinessException("UserService::updateUserRolesById - Execution failed.");
        }
    }

    private Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
