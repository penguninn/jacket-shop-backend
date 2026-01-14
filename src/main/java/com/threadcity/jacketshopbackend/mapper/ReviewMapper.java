package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.review.response.ReviewResponse;
import com.threadcity.jacketshopbackend.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Mapper cho Review entity → ReviewResponse DTO
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "rating", source = "rating")
    @Mapping(target = "comment", source = "comment")
    @Mapping(target = "createdAt", expression = "java(mapToLocalDateTime(review.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(mapToLocalDateTime(review.getUpdatedAt()))")
    ReviewResponse toDto(Review review);

    /**
     * Map danh sách Review → danh sách ReviewResponse
     * Dùng trong getAllReviews khi stream map page content
     */
    List<ReviewResponse> toDtoList(List<Review> reviews);

    // Converter chung cho Instant → LocalDateTime (tái sử dụng được)
    default LocalDateTime mapToLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}