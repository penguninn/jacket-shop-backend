package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShippingInfoRequest {
    @NotNull(message = "Carrier name is required")
    private String carrierName;
    
    @NotNull(message = "Carrier service name is required")
    private String carrierServiceName;
    
    private BigDecimal shippingFee;
}
