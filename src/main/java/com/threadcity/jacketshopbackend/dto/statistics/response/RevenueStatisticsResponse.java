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
public class RevenueStatisticsResponse {

    private BigDecimal totalRevenue;

    private BigDecimal totalProfit;

    private BigDecimal totalCost;

    private Long totalCompletedOrders;
}
