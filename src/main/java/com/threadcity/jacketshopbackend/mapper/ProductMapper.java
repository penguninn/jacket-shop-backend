package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.ProductResponse;
import com.threadcity.jacketshopbackend.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        BrandMapper.class,
        StyleMapper.class
})
public interface ProductMapper {

    ProductResponse toDto(Product product);
}
