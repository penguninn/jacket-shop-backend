package com.threadcity.jacketshopbackend.dto.statistics.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingProductResponse {

    private Long productId;

    private String productName;

    private String thumbnail;

    private String brandName;

    private Long soldCount;

    private BigDecimal revenue;
}
