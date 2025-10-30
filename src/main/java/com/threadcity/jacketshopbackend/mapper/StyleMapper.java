package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.request.StyleRequest;
import com.threadcity.jacketshopbackend.dto.response.StyleResponse;
import com.threadcity.jacketshopbackend.entity.Style;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StyleMapper {
    StyleResponse toDto(Style style);

    Style toEntity(StyleRequest request);
}
