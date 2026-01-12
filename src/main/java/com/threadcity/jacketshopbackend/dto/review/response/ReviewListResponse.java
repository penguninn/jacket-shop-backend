package com.threadcity.jacketshopbackend.dto.review.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListResponse {

    private List<ReviewResponse> items;
}
