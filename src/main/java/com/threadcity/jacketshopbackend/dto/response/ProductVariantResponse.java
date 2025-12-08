package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.entity.Color;
import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.Size;
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
    private Product product;
    private String sku;
    private Size size;
    private Color color;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    @Builder.Default
    private Integer quantity = 0;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}
