package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.response.RoleResponse;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.mapper.RoleMapper;
import com.threadcity.jacketshopbackend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public List<RoleResponse> getAllRoles() {
        log.info("RoleService::getAllRoles - Execution started.");
        List<Role> roles = roleRepository.findAll();
        List<RoleResponse> roleResponses = roles.stream()
                .map(roleMapper::toDto)
                .toList();
        log.info("RoleService::getAllRoles - Execution completed.");
        return roleResponses;
    }
}
