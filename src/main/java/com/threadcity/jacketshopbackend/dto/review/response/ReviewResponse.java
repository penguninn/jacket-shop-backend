package com.threadcity.jacketshopbackend.dto.review.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;

    private Long productId;
    private String productName;

    private Long userId;
    private String userName;

    private Long orderId;

    private Integer rating;
    private String comment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;  // optional, nếu DTO cần hiển thị
}