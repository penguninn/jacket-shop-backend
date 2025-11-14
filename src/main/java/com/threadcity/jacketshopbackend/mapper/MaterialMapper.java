package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.request.MaterialRequest;
import com.threadcity.jacketshopbackend.dto.response.MaterialResponse;
import com.threadcity.jacketshopbackend.entity.Material;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MaterialMapper {
    MaterialResponse toDto(Material material);

    Material toEntity(MaterialRequest request);
}
