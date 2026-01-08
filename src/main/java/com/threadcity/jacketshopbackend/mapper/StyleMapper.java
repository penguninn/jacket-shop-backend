package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.product.request.StyleCreateRequest;
import com.threadcity.jacketshopbackend.dto.product.request.StyleUpdateRequest;
import com.threadcity.jacketshopbackend.dto.product.response.StyleResponse;
import com.threadcity.jacketshopbackend.entity.Style;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StyleMapper {
    StyleResponse toDto(Style style);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Style toEntity(StyleCreateRequest request);
}
