package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderCode;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String shippingRecipientName;
    private String shippingRecipientPhone;
    private String shippingAddressLine;
    private String shippingProvinceName;
    private String shippingDistrictName;
    private String shippingWardName;
    private String paymentMethodName;
    private PaymentStatus paymentStatus;
    private String shippingMethodName;
    private BigDecimal shippingFee;
    private String couponCode;
    private BigDecimal discount;
    private BigDecimal subtotal;
    private BigDecimal total;
    private OrderStatus status;
    private String note;
    private Instant createdAt;
    private List<OrderDetailResponse> details;
}
