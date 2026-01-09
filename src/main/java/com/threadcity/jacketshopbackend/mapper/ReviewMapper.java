//package com.threadcity.jacketshopbackend.mapper;
//
//import com.threadcity.jacketshopbackend.dto.product.response.ReviewResponse;
//import com.threadcity.jacketshopbackend.entity.Review;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//
//@Mapper(componentModel = "spring")
//public interface ReviewMapper {
//
//    @Mapping(source = "product.id", target = "productId")
//    @Mapping(source = "productName", target = "productName")
//    @Mapping(source = "userName", target = "userName")
//    @Mapping(source = "createdAt", target = "createdAt")
//    ReviewResponse toDto(Review review);
//}
//
package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.review.response.ReviewResponse;
import com.threadcity.jacketshopbackend.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "orderId", source = "order.id")
    ReviewResponse toDto(Review review);
}