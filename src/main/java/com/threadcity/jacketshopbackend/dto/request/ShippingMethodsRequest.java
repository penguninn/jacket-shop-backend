package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShippingMethodsRequest {

    @NotNull(message = "Name cannot be null")
    @Size(max = 100, message = "Name too long")
    private String name;

    @Size(max = 255, message = "Description too long")
    private String description;

    @NotNull(message = "Fee cannot be null")
    private BigDecimal fee;

    @Min(value = 1, message = "Estimated days must be >= 1")
    @Max(value = 60, message = "Estimated days must be <= 60")
    private Integer estimatedDays;

    private Enums.Status status;
}
