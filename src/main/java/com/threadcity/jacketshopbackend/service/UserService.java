package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProfileResponse;
import com.threadcity.jacketshopbackend.dto.response.UserReponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.ProfileUpdateRequest;
import com.threadcity.jacketshopbackend.dto.request.UserBulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.UserBulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.UserCreateRequest;
import com.threadcity.jacketshopbackend.dto.request.UserFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.UserRolesRequset;
import com.threadcity.jacketshopbackend.dto.request.UserStatusRequset;
import com.threadcity.jacketshopbackend.dto.request.UserUpdateRequset;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.UserMapper;
import com.threadcity.jacketshopbackend.repository.RefreshTokenRepository;
import com.threadcity.jacketshopbackend.repository.RoleRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import com.threadcity.jacketshopbackend.specification.UserSpecification;

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
    private final RefreshTokenRepository refreshTokenRepository;

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

    public PageResponse<?> getAllUsers(UserFilterRequest request) {
        log.info("UserService::getAllUsers - Execution started.");
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Specification<User> spec = UserSpecification.buildSpec(request);
            Page<User> userPage = userRepository.findAll(spec, pageable);

            List<UserReponse> usersResponse = userPage.getContent().stream()
                    .map(userMapper::toUserReponse)
                    .toList();
            log.info("UserService::getAllUsers - Execution completed.");
            return PageResponse.builder()
                    .contents(usersResponse)
                    .size(request.getSize())
                    .page(request.getPage())
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
        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            throw new BusinessException("At least one role is required");
        }
        List<Role> roles = roleRepository.findAllById(request.getRoleIds());
        if (roles.size() != request.getRoleIds().size()) {
            Set<Long> foundIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getRoleIds());
            missingIds.removeAll(foundIds);
            throw new EntityNotFoundException("Roles not found: " + missingIds);
        }
        try {
            User user = User.builder()
                    .username(request.getUsername())
                    .fullName(request.getFullName())
                    .phone(request.getPhone())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .status(request.getStatus())
                    .build();
            user.getRoles().addAll(roles);
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
        log.info("UserService::updateUserById - Execution started.");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with userId: " + id));

        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            throw new BusinessException("At least one role is required");
        }

        Set<Long> requestedIds = new HashSet<>(request.getRoleIds());
        List<Role> roles = roleRepository.findAllById(requestedIds);

        if (roles.size() != requestedIds.size()) {
            Set<Long> foundIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(requestedIds);
            missingIds.removeAll(foundIds);
            throw new EntityNotFoundException("Roles not found: " + missingIds);
        }

        try {
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
            user.setStatus(request.getStatus());
            user.setRoles(new HashSet<>(roles));

            User userSaved = userRepository.save(user);
            log.info("UserService::updateUserById - Execution completed. [UserId: {}]", id);
            return userMapper.toUserReponse(userSaved);
        } catch (RuntimeException e) {
            log.error("UserService::updateUserById - Execution failed.", e);
            throw new BusinessException("UserService::updateUserById - Execution failed.");
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

    @Transactional
    public void deleteUserById(Long id) {
        log.info("UserService::deleteUserById - Execution started. [UserId: {}]", id);
        Long currentUserId = getUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException("Cannot delete your own account");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with userId: " + id));
        try {
            refreshTokenRepository.deleteByUserId(id);
            userRepository.delete(user);
            log.info("UserService::deleteUserById - Execution completed. [UserId: {}]", id);
        } catch (RuntimeException e) {
            log.error("UserService::deleteUserById - Execution failed.", e);
            throw new BusinessException("UserService::deleteUserById - Execution failed.");
        }
    }

    @Transactional
    public List<UserReponse> updateUsersStatusBulk(UserBulkStatusRequest request) {
        log.info("UserService::updateUsersStatusBulk - Execution started.");
        List<User> users = userRepository.findAllById(request.getIds());
        if (users.size() != request.getIds().size()) {
            Set<Long> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new EntityNotFoundException("Users not found: " + missingIds);
        }
        try {
            users.forEach(user -> user.setStatus(request.getStatus()));
            List<User> savedUsers = userRepository.saveAll(users);
            log.info("UserService::updateUsersStatusBulk - Execution completed.");
            return savedUsers.stream().map(userMapper::toUserReponse).toList();
        } catch (RuntimeException e) {
            log.error("UserService::updateUsersStatusBulk - Execution failed.", e);
            throw new BusinessException("UserService::updateUsersStatusBulk - Execution failed.");
        }
    }

    @Transactional
    public void deleteUsersBulk(UserBulkDeleteRequest request) {
        log.info("UserService::deleteUsersBulk - Execution started.");
        Long currentUserId = getUserId();
        if (request.getIds().contains(currentUserId)) {
            throw new BusinessException("Cannot delete your own account");
        }
        List<User> users = userRepository.findAllById(request.getIds());
        if (users.size() != request.getIds().size()) {
            Set<Long> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new EntityNotFoundException("Users not found: " + missingIds);
        }
        try {
            refreshTokenRepository.deleteByUserIdIn(request.getIds());
            userRepository.deleteAllInBatch(users);
            log.info("UserService::deleteUsersBulk - Execution completed.");
        } catch (RuntimeException e) {
            log.error("UserService::deleteUsersBulk - Execution failed.", e);
            throw new BusinessException("UserService::deleteUsersBulk - Execution failed.");
        }
    }

    private Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
