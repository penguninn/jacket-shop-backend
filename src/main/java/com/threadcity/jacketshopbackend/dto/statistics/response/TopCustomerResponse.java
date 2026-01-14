package com.threadcity.jacketshopbackend.dto.statistics.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerResponse {

    private Long userId;

    private String fullName;

    private String phone;

    private String email;

    private String avatar;

    private BigDecimal totalSpent;

    private Long orderCount;
}
