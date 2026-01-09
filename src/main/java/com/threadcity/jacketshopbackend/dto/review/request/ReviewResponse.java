package com.threadcity.jacketshopbackend.dto.review.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
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
    private Instant createdAt;
    private Instant updatedAt;
}
