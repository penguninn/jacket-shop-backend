package com.threadcity.jacketshopbackend.dto.order.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderHistoryResponse implements Serializable {
    private Long id;
    private Long orderId;
    private Enums.OrderStatus oldStatus;
    private Enums.OrderStatus newStatus;
    private Enums.PaymentStatus oldPaymentStatus;
    private Enums.PaymentStatus newPaymentStatus;
    private Long changedByUserId;
    private String changedByUserName;
    private String note;
    private Instant createdAt;
}
