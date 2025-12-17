package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class CouponUpdateRequest {

    @Size(max = 255, message = "Description too long")
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

    @Min(value = 0, message = "Usage limit cannot be negative")
    private Integer usageLimit;

    @Min(value = 0, message = "Used count cannot be negative")
    private Integer usedCount;

    @NotNull(message = "Valid From Date is required")
    private Instant validFrom;

    @NotNull(message = "Valid To Date is required")
    private Instant validTo;

    @NotNull(message = "Status is required")
    private Enums.Status status;
}
