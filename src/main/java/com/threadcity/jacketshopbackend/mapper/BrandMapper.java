package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.product.request.BrandCreateRequest;
import com.threadcity.jacketshopbackend.dto.product.response.BrandResponse;
import com.threadcity.jacketshopbackend.entity.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandResponse toDto(Brand brand);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Brand toEntity(BrandCreateRequest request);
}
