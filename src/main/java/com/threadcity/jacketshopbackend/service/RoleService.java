package com.threadcity.jacketshopbackend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.dto.response.RoleResponse;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.RoleMapper;
import com.threadcity.jacketshopbackend.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public List<RoleResponse> getAllRoles() {
        log.info("RoleService::getAllRoles - Execution started.");
        try {
            List<Role> roles = roleRepository.findAll();
            List<RoleResponse> roleResponses = roles.stream()
                    .map(roleMapper::toDto)
                    .toList();
            log.info("RoleService::getAllRoles - Execution completed.");
            return roleResponses;
        } catch (Exception e) {
            log.error("RoleService::getAllRoles - Execution failed.", e);
            throw new BusinessException("RoleService::getAllRoles - Execution failed.");
        }
    }
}
