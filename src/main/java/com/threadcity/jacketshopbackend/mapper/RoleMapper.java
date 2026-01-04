package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.auth.response.RoleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toDto(Role role);
}
