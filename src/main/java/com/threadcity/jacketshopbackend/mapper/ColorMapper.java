package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.attribute.request.ColorCreateRequest;
import com.threadcity.jacketshopbackend.dto.attribute.request.ColorUpdateRequest;
import com.threadcity.jacketshopbackend.dto.attribute.response.ColorResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ColorMapper {
    ColorResponse toDto(Color color);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Color toEntity(ColorCreateRequest request);
}
