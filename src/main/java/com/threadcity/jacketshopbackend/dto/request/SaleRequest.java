package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SaleRequest {

    @NotNull(message = "Variant IDs are required")
    private List<Long> productVariantIds;

    private String name;

    private String description;

    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;

    @Min(value = 0, message = "Discount percentage cannot be negative")
    @Max(value = 100, message = "Discount percentage cannot exceed 100")
    private BigDecimal discountPercentage;
}
