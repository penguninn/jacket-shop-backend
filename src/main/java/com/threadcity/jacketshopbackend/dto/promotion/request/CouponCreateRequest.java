package com.threadcity.jacketshopbackend.dto.promotion.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class CouponCreateRequest implements Serializable {

    @NotBlank(message = "Code cannot be empty")
    @Size(max = 50, message = "Code must be less than 50 characters")
    private String code;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Coupon Type is required")
    private Enums.CouponType type;

    @NotNull(message = "Value is required")
    @Min(value = 0, message = "Value cannot be negative")
    private BigDecimal value;

    @Min(value = 0, message = "Min order value cannot be negative")
    private BigDecimal minOrderValue;

    @Min(value = 0, message = "Max discount cannot be negative")
    private BigDecimal maxDiscount;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @Min(value = 1, message = "Usage limit per user must be at least 1")
    private Integer usageLimitPerUser;

    @NotNull(message = "Valid From Date is required")
    private Instant validFrom;

    @NotNull(message = "Valid To Date is required")
    private Instant validTo;

    @NotNull(message = "Status is required")
    private Enums.Status status;

    @AssertTrue(message = "Valid from date must be before valid to date")
    public boolean isValidDateRange() {
        if (validFrom == null || validTo == null) {
            return true; // Let @NotNull handle null validation
        }
        return validFrom.isBefore(validTo);
    }
}
