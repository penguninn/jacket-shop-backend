package com.threadcity.jacketshopbackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SaleResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal discountPercentage;
    private List<SaleVariantDetail> variants;

    @Data
    @Builder
    public static class SaleVariantDetail {
        private Long variantId;
        private String productName;
        private String sku;
        private String image;
        private BigDecimal originalPrice;
        private BigDecimal salePrice;
    }
}