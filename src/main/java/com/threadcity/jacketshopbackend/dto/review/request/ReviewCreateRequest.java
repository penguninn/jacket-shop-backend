package com.threadcity.jacketshopbackend.dto.review.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {

    @NotNull(message = "Product ID là bắt buộc")
    private Long productId;

    @NotNull(message = "Order ID là bắt buộc")
    private Long orderId;

    @NotNull(message = "Điểm đánh giá là bắt buộc")
    @Min(1) @Max(5)
    private Integer rating;

    private String comment;
}