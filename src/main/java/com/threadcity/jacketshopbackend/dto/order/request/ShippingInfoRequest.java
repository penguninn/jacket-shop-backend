package com.threadcity.jacketshopbackend.dto.order.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ShippingInfoRequest implements Serializable {
    @NotNull(message = "Carrier name is required")
    private String carrierName;

    @NotNull(message = "Carrier service name is required")
    private String carrierServiceName;

    private String carrierRateId;

    private String deliveryTimeEstimate;

    private BigDecimal shippingFee;
}
