package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.request.ColorRequest;
import com.threadcity.jacketshopbackend.dto.response.ColorResponse;
import com.threadcity.jacketshopbackend.entity.Color;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ColorMapper {
    ColorResponse toDto(Color color);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Color toEntity(ColorRequest request);
}
