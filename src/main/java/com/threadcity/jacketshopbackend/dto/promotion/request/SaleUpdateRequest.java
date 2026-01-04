package com.threadcity.jacketshopbackend.dto.promotion.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class SaleUpdateRequest {

    @NotNull(message = "ID is required")
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.00", message = "Discount percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "Discount percentage must not exceed 100")
    private BigDecimal discountPercentage;

    @NotNull(message = "Start date is required")
    private OffsetDateTime startDate;

    @NotNull(message = "End date is required")
    private OffsetDateTime endDate;

    @NotNull(message = "Status is required")
    private Enums.Status status;
}
