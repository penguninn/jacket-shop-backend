package com.threadcity.jacketshopbackend.dto.statistics.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutOfStockProductResponse {

    private Long variantId;

    private Long productId;

    private String productName;

    private String sku;

    private String colorName;

    private String sizeName;

    private String materialName;

    private String image;

    private Integer availableQuantity;
}
