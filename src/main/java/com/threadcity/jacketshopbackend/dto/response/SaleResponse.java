package com.threadcity.jacketshopbackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SaleResponse {
    private Long variantId;
    private String productName;
    private String sku;
    private String image;
    private BigDecimal originalPrice;
    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;
    private BigDecimal salePrice;
    private BigDecimal discountPercentage;
}
