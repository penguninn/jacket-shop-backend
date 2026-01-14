package com.threadcity.jacketshopbackend.dto.statistics.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {

    private RevenueStatisticsResponse revenue;

    private List<OrderStatusStatisticsResponse> ordersByStatus;

    private OrderTypeStatisticsResponse completedOrdersByType;

    private List<TopSellingProductResponse> topSellingProducts;

    private List<OutOfStockProductResponse> outOfStockProducts;

    private List<TopRatedProductResponse> topRatedProducts;

    private InventoryValueResponse inventoryValue;

    private CustomerStatisticsResponse customerStatistics;

    private List<TopCustomerResponse> topCustomers;
}
