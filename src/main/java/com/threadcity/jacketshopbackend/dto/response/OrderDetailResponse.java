package com.threadcity.jacketshopbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse implements Serializable {

    private Long id;

    private Long productId;

    private Long productVariantId;

    private String productName;

    private String size;

    private String color;

    private String material;

    private String sku;
    
    private String image;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private BigDecimal discountPercentage;

    private Integer quantity;

    private BigDecimal subtotal;
}
