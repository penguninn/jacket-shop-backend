package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.attribute.request.MaterialCreateRequest;
import com.threadcity.jacketshopbackend.dto.attribute.request.MaterialUpdateRequest;
import com.threadcity.jacketshopbackend.dto.attribute.response.MaterialResponse;
import com.threadcity.jacketshopbackend.entity.Material;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MaterialMapper {
    MaterialResponse toDto(Material material);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Material toEntity(MaterialCreateRequest request);
}
