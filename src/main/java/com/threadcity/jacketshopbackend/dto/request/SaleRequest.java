package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SaleRequest {

    @NotNull(message = "Variant ID is required")
    private Long variantId;

    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;

    @Min(value = 0, message = "Discount percentage cannot be negative")
    @Max(value = 100, message = "Discount percentage cannot exceed 100")
    private BigDecimal discountPercentage;
}
