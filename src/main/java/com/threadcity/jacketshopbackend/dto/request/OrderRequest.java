package com.threadcity.jacketshopbackend.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {

    @NotNull(message = "Order Type is required")
    private OrderType orderType;

    @NotNull(message = "Payment method ID is required")
    private Long paymentMethodId;

    private String note;

    private String couponCode;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;

    private Long userId;
    private String customerName;
    private String customerPhone;

    private Long addressId;

    private String carrierName;
    private String carrierServiceName;
    private String carrierRateId;
    private String deliveryTimeEstimate;
    private BigDecimal shippingFee;

    private String transactionId;

}
