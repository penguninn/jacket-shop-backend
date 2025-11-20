package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.entity.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
@Data
@Builder
public class ProductVariantResponse implements Serializable {
    private Long id;
    private Product product;
    private String sku;
    private Size size;
    private Color color;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private Integer quantity = 0;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}
