package com.threadcity.jacketshopbackend.dto.review.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateRequest {

    @Min(1) @Max(5)
    private Integer rating;

    private String comment;
}