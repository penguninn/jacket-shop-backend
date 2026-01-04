package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.request.SizeRequest;
import com.threadcity.jacketshopbackend.dto.response.SizeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SizeMapper {
    SizeResponse toDto(Size size);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Size toEntity(SizeRequest request);
}
