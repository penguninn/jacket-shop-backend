package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockAdjustmentRequest {
    @NotNull(message = "Quantity change is required")
    private Integer quantityChange;
}
