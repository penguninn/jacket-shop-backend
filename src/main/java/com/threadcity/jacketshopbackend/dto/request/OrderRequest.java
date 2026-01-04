package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest {

    @NotNull(message = "Order Type is required")
    private OrderType orderType;

    private Long paymentMethodId;

    private String note;

    private String couponCode;

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
