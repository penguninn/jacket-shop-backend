package com.threadcity.jacketshopbackend.dto.order.request;

import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest implements Serializable {

    @NotNull(message = "Order Type is required")
    private OrderType orderType;

    @NotNull(message = "Payment method Id is required")
    private Long paymentMethodId;

    private String note;

    private String couponCode;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<OrderItemRequest> items;

    private Long userId;

    @NotNull(message = "Address Id is required")
    private Long addressId;

    private BigDecimal shippingFee;

    private String carrierName;

    private String carrierServiceName;

    private String carrierRateId;

    private String deliveryTimeEstimate;
}
