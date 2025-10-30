package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.request.BrandRequest;
import com.threadcity.jacketshopbackend.dto.response.BrandResponse;
import com.threadcity.jacketshopbackend.entity.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandResponse toDto(Brand brand);

    Brand toEntity(BrandRequest request);
}
