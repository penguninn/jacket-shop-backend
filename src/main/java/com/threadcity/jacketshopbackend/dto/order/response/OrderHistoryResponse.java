package com.threadcity.jacketshopbackend.dto.order.response;

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
    private String oldStatus;
    private String newStatus;
    private String oldPaymentStatus;
    private String newPaymentStatus;
    private Long changedByUserId;
    private String changedByUserName;
    private String note;
    private Instant createdAt;
}
