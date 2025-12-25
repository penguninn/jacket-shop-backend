package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.ReviewResponse;
import com.threadcity.jacketshopbackend.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "productName", target = "productName")
    @Mapping(source = "userName", target = "userName")
    @Mapping(source = "createdAt", target = "createdAt")
    ReviewResponse toDto(Review review);
}

