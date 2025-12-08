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
    private ProductResponse product;
    private SizeResponse size;
    private ColorResponse color;
    private MaterialResponse material;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private Integer quantity;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}
