package com.threadcity.jacketshopbackend.dto.promotion.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class SaleCreateRequest implements Serializable {

    @NotNull(message = "Variant IDs are required")
    private List<Long> productVariantIds;

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Start date is required")
    private Instant startDate;

    @NotNull(message = "End date is required")
    private Instant endDate;

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.00", message = "Discount percentage must be at least 0")
    @DecimalMax(value = "100.00", message = "Discount percentage must not exceed 100")
    private BigDecimal discountPercentage;

    @NotNull(message = "Status is required")
    private Enums.Status status;

    @AssertTrue(message = "Start date must be before end date")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return startDate.isBefore(endDate);
    }
}
