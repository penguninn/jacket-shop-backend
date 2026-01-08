package com.threadcity.jacketshopbackend.dto.order.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryResponse implements Serializable {

    private Long id;
    private String orderCode;
    private Enums.OrderType orderType;
    private Enums.OrderStatus status;
    private Enums.PaymentStatus paymentStatus;
    private String customerName;
    private String customerPhone;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private Integer itemCount;
    private Instant createdAt;
    private Instant updatedAt;
}
