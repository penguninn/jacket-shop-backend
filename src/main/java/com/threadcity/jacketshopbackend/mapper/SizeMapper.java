package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.attribute.request.SizeCreateRequest;
import com.threadcity.jacketshopbackend.dto.attribute.request.SizeUpdateRequest;
import com.threadcity.jacketshopbackend.dto.attribute.response.SizeResponse;
import com.threadcity.jacketshopbackend.entity.Size;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SizeMapper {
    SizeResponse toDto(Size size);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Size toEntity(SizeCreateRequest request);
}
