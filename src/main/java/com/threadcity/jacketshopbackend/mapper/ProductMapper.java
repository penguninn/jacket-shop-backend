package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.product.response.ProductResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        BrandMapper.class,
        StyleMapper.class,
        ColorMapper.class,
        MaterialMapper.class,
        SizeMapper.class
})
public interface ProductMapper {

    ProductResponse toDto(Product product);
}
