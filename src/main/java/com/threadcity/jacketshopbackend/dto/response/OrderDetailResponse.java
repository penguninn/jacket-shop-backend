package com.threadcity.jacketshopbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {
    private Long id;
    private Long productVariantId;
    private String productName;
    private String size;
    private String color;
    private String sku;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
