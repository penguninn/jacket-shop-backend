package com.threadcity.jacketshopbackend.dto.product.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse implements Serializable {
    private Long id;
    private Long productId;
    private String productName;
    private String userName;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}
