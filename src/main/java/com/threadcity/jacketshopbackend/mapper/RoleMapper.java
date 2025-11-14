package com.threadcity.jacketshopbackend.mapper;

import org.mapstruct.Mapper;

import com.threadcity.jacketshopbackend.dto.response.RoleResponse;
import com.threadcity.jacketshopbackend.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toDto(Role role);
}
