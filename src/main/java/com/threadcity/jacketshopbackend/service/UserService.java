package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.*;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProfileResponse;
import com.threadcity.jacketshopbackend.dto.response.UserResponse;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.filter.UserFilterRequest;
import com.threadcity.jacketshopbackend.mapper.UserMapper;
import com.threadcity.jacketshopbackend.repository.RefreshTokenRepository;
import com.threadcity.jacketshopbackend.repository.RoleRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import com.threadcity.jacketshopbackend.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found"));
        log.info("UserService::getProfile execution ended");
        return userMapper.toProfile(user);
    }

    public ProfileResponse updateProfile(ProfileUpdateRequest request) {
        log.info("UserService::updateProfile execution started");
        Long userId = getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found"));
        user.setFullName(request.getFullName());
        userRepository.save(user);
        log.info("UserService::updateProfile execution ended");
        return userMapper.toProfile(user);
    }

    public UserResponse getUserById(Long Id) {
        log.info("UserService::getUserById - Execution started. [Id: {}]", Id);
        User user = userRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND,
                        "User not found with id: " + Id));
        log.info("UserService::getUserById - Execution completed. [UserId: {}]", Id);
        return userMapper.toUserResponse(user);
    }

    public PageResponse<?> getAllUsers(UserFilterRequest request) {
        log.info("UserService::getAllUsers - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<User> spec = UserSpecification.buildSpec(request);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserResponse> usersResponse = userPage.getContent().stream()
                .map(userMapper::toUserResponse)
                .toList();
        log.info("UserService::getAllUsers - Execution completed.");
        return PageResponse.builder()
                .contents(usersResponse)
                .size(request.getSize())
                .page(request.getPage())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements()).build();
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("UserService::createUser - Execution started.");
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResourceConflictException(ErrorCodes.USER_USERNAME_DUPLICATE, "Username already exists");
        }
        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "At least one role is required");
        }
        List<Role> roles = roleRepository.findAllById(request.getRoleIds());
        if (roles.size() != request.getRoleIds().size()) {
            Set<Long> foundIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getRoleIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.ROLE_NOT_FOUND, "Roles not found: " + missingIds);
        }

        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(request.getStatus())
                .build();
        user.getRoles().addAll(roles);
        User saved = userRepository.save(user);

        log.info("UserService::createUser - Execution completed.");
        return userMapper.toUserResponse(saved);
    }

    @Transactional
    public UserResponse updateUserById(UserUpdateRequest request, Long id) {
        log.info("UserService::updateUserById - Execution started.");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND,
                        "User not found with id: " + id));

        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "At least one role is required");
        }

        Set<Long> requestedIds = new HashSet<>(request.getRoleIds());
        List<Role> roles = roleRepository.findAllById(requestedIds);

        if (roles.size() != requestedIds.size()) {
            Set<Long> foundIds = roles.stream().map(Role::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(requestedIds);
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.ROLE_NOT_FOUND, "Roles not found: " + missingIds);
        }

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus());
        user.setRoles(new HashSet<>(roles));

        User userSaved = userRepository.save(user);
        log.info("UserService::updateUserById - Execution completed. [UserId: {}]", id);
        return userMapper.toUserResponse(userSaved);
    }

    @Transactional
    public UserResponse updateUserStatusById(UpdateStatusRequest request, Long id) {
        log.info("UserService::updateUserStatusById - Execution started.");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND,
                        "User not found with id: " + id));

        user.setStatus(request.getStatus());
        User userSaved = userRepository.save(user);
        log.info("UserService::updateUserStatusById - Execution completed. [UserId: {}]", id);
        return userMapper.toUserResponse(userSaved);
    }

    @Transactional
    public UserResponse updateUserRolesById(UserUpdateRolesRequest request, Long id) {
        log.info("UserService::updateUserRolesById - Execution started.");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND,
                        "User not found with id: " + id));

        Set<Role> roles = roleRepository.findAllById(request.getRoleIds()).stream().collect(Collectors.toSet());
        if (roles.size() != request.getRoleIds().size()) {
            Set<Long> foundIds = roles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());
            Set<Long> notFoundIds = new HashSet<>(request.getRoleIds());
            notFoundIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.ROLE_NOT_FOUND, "Roles not found: " + notFoundIds);
        }

        user.setRoles(roles);
        User userSaved = userRepository.save(user);
        log.info("UserService::updateUserRolesById - Execution completed. [UserId: {}]", id);
        return userMapper.toUserResponse(userSaved);
    }

    @Transactional
    public void deleteUserById(Long id) {
        log.info("UserService::deleteUserById - Execution started. [UserId: {}]", id);
        Long currentUserId = getUserId();
        if (id.equals(currentUserId)) {
            throw new InvalidRequestException(ErrorCodes.USER_SELF_DELETE, "Cannot delete your own account");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND,
                        "User not found with id: " + id));

        refreshTokenRepository.deleteByUserId(id);
        userRepository.delete(user);
        log.info("UserService::deleteUserById - Execution completed. [UserId: {}]", id);
    }

    @Transactional
    public List<UserResponse> bulkUpdateUsersStatus(BulkStatusRequest request) {
        log.info("UserService::bulkUpdateUsersStatus - Execution started.");
        List<User> users = userRepository.findAllById(request.getIds());
        if (users.size() != request.getIds().size()) {
            Set<Long> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "Users not found: " + missingIds);
        }

        users.forEach(user -> user.setStatus(request.getStatus()));
        List<User> savedUsers = userRepository.saveAll(users);
        log.info("UserService::bulkUpdateUsersStatus - Execution completed.");
        return savedUsers.stream().map(userMapper::toUserResponse).toList();
    }

    @Transactional
    public void bulkDeleteUsers(BulkDeleteRequest request) {
        log.info("UserService::deleteUsersBulk - Execution started.");
        Long currentUserId = getUserId();
        if (request.getIds().contains(currentUserId)) {
            throw new InvalidRequestException(ErrorCodes.USER_SELF_DELETE, "Cannot delete your own account");
        }
        List<User> users = userRepository.findAllById(request.getIds());
        if (users.size() != request.getIds().size()) {
            Set<Long> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "Users not found: " + missingIds);
        }

        refreshTokenRepository.deleteByUserIdIn(request.getIds());
        userRepository.deleteAllInBatch(users);
        log.info("UserService::deleteUsersBulk - Execution completed.");
    }

    private Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}
