package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.product.response.ProductResponse;
import com.threadcity.jacketshopbackend.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {
        BrandMapper.class,
        StyleMapper.class,
        ColorMapper.class,
        MaterialMapper.class,
        SizeMapper.class
})
public interface ProductMapper {

    @Mapping(target = "variantsCount", expression = "java(product.getVariants() != null ? product.getVariants().size() : 0)")
    @Mapping(target = "colors", ignore = true)
    @Mapping(target = "materials", ignore = true)
    @Mapping(target = "sizes", ignore = true)
    ProductResponse toDto(Product product);
}
