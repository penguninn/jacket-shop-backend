package com.threadcity.jacketshopbackend.dto.product.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Filter request for reviews with comprehensive filtering options
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFilterRequest implements Serializable {

    private Long productId;

    private Long userId;

    @Min(value = 1, message = "Minimum rating must be at least 1")
    @Max(value = 5, message = "Minimum rating must be at most 5")
    private Integer minRating;

    @Min(value = 1, message = "Maximum rating must be at least 1")
    @Max(value = 5, message = "Maximum rating must be at most 5")
    private Integer maxRating;

    private List<Integer> ratings;

    private Instant startDate;

    private Instant endDate;

    private String search;

    @Builder.Default
    @Min(value = 0, message = "Page must be non-negative")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must not exceed 100")
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDir = "DESC";
}
