package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.entity.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductVariantRequest {
    @NotNull(message = "Sku cannot be null")
    private Long product;
    private String sku;
    private Long size;
    private Long color;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private Integer quantity = 0;
    private Enums.Status status;
}
