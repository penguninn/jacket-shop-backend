package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.RoleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toDto(Role role);
}
