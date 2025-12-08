package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        ProductMapper.class,
        SizeMapper.class,
        ColorMapper.class,
        MaterialMapper.class
})
public interface ProductVariantMapper {

    ProductVariantResponse toDto(ProductVariant productVariant);
}
