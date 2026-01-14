package com.threadcity.jacketshopbackend.dto.review.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFilterRequest {

    private Long productId;
    private Long userId;
    private Long orderId;

    private Integer rating;
    private Integer minRating;
    private Integer maxRating;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDir = "DESC";
}
