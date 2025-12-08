package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductVariantRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Size(max = 255, message = "SKU must be less than 255 characters")
    private String sku;

    @NotNull(message = "Size ID is required")
    private Long sizeId;

    @NotNull(message = "Color ID is required")
    private Long colorId;

    @NotNull(message = "Material ID is required")
    private Long materialId;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "Cost price is required")
    @Min(value = 0, message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    @Min(value = 0, message = "Sale price cannot be negative")
    private BigDecimal salePrice;

    @Builder.Default
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity = 0;

    @NotNull(message = "Status is required")
    private Enums.Status status;
}
