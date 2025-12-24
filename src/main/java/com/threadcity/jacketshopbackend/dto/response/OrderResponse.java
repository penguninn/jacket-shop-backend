package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse implements Serializable {

    private Long id;

    private String orderCode;

    private OrderType orderType;

    private Long userId;

    private String customerName;

    private String customerPhone;

    private Long staffId;

    private String staffName;

    private String shippingRecipientName;

    private String shippingRecipientPhone;

    private String shippingAddressLine;

    private String shippingProvinceCode;

    private String shippingDistrictCode;

    private String shippingWardCode;

    private String shippingProvinceName;

    private String shippingDistrictName;

    private String shippingWardName;

    private Long paymentMethodId;

    private String paymentMethodName;

    private PaymentStatus paymentStatus;

    private String carrierName;

    private String carrierServiceName;

    private BigDecimal shippingFee;

    private String couponCode;

    private BigDecimal discount;

    private BigDecimal subtotal;

    private BigDecimal total;

    private String transactionId;

    private Instant paymentDate;

    private OrderStatus status;

    private String note;

    private Instant createdAt;

    private List<OrderDetailResponse> details;
}
