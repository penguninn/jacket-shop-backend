package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.product.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "product.id", target = "productId")
    ReviewResponse toDto(Review review);
}
