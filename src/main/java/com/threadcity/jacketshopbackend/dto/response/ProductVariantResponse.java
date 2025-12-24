package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantResponse implements Serializable {

    private Long id;
    private String sku;
    private Long productId;
    private String productName;
    private SizeResponse size;
    private ColorResponse color;
    private MaterialResponse material;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer quantity;
    private Enums.Status status;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String image;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Integer soldCount;
    private Integer returnCount;
    private BigDecimal salePrice;
    private BigDecimal discountPercentage;
    private Instant createdAt;
    private Instant updatedAt;
}
