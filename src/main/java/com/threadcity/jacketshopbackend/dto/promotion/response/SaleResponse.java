package com.threadcity.jacketshopbackend.dto.promotion.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class SaleResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private BigDecimal discountPercentage;
    private Enums.Status status;
    private List<SaleVariantDetail> variants;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    public static class SaleVariantDetail implements Serializable {
        private Long variantId;
        private String productName;
        private String sku;
        private String image;
        private BigDecimal originalPrice;
        private BigDecimal salePrice;
    }
}