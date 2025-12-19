package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductVariantUpdateRequest {

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "Cost price is required")
    @Min(value = 0, message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    @Builder.Default
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity = 0;

    @NotNull(message = "Status is required")
    private Enums.Status status;

    private String image;

    @Min(value = 0, message = "Weight cannot be negative")
    private BigDecimal weight;

    @Min(value = 0, message = "Length cannot be negative")
    private BigDecimal length;

    @Min(value = 0, message = "Width cannot be negative")
    private BigDecimal width;

    @Min(value = 0, message = "Height cannot be negative")
    private BigDecimal height;
}
