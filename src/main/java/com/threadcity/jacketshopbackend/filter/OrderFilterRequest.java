package com.threadcity.jacketshopbackend.filter;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class OrderFilterRequest {
    private int page = 0;
    private int size = 10;
    private String sortBy = "createdAt";
    private String sortDir = "desc";

    private String orderCode;
    private Instant startDate;
    private Instant endDate;
    private OrderType orderType;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private Long userId;
    private Long staffId;
}
