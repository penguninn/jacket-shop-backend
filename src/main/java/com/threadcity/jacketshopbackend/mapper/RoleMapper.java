package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.auth.request.RoleCreateRequest;
import com.threadcity.jacketshopbackend.dto.auth.request.RoleUpdateRequest;
import com.threadcity.jacketshopbackend.dto.auth.response.RoleResponse;
import com.threadcity.jacketshopbackend.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role toEntity(RoleCreateRequest request);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "users", ignore = true)
    void updateEntity(RoleUpdateRequest request, @MappingTarget Role entity);

    RoleResponse toResponse(Role entity);

    List<RoleResponse> toResponseList(List<Role> entities);

    Set<RoleResponse> toResponseSet(Set<Role> entities);
}
