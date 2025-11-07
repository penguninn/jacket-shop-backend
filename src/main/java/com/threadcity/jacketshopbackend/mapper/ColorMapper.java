package com.threadcity.jacketshopbackend.mapper;


import com.threadcity.jacketshopbackend.dto.request.ColorRequest;

import com.threadcity.jacketshopbackend.dto.response.ColorResponse;

import com.threadcity.jacketshopbackend.entity.Color;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ColorMapper {
    ColorResponse toDto(Color color);
    Color toEntity(ColorRequest request);
}
