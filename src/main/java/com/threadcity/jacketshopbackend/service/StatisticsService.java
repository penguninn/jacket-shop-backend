package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.statistics.response.*;
import com.threadcity.jacketshopbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    /**
     * Get revenue statistics: total revenue, total profit, total cost
     */
    public RevenueStatisticsResponse getRevenueStatistics(Instant startDate, Instant endDate) {
        log.info("StatisticsService::getRevenueStatistics - Execution started. [startDate: {}, endDate: {}]",
                startDate, endDate);

        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(startDate, endDate);
        BigDecimal totalCost = orderDetailRepository.calculateTotalCost(startDate, endDate);
        BigDecimal totalProfit = totalRevenue.subtract(totalCost);

        // Count completed orders
        List<Object[]> ordersByStatus = orderRepository.countOrdersByStatus(startDate, endDate);
        long completedOrders = ordersByStatus.stream()
                .filter(row -> row[0] == Enums.OrderStatus.COMPLETED)
                .map(row -> (Long) row[1])
                .findFirst()
                .orElse(0L);

        log.info("StatisticsService::getRevenueStatistics - Execution completed. " +
                        "[revenue: {}, cost: {}, profit: {}, completedOrders: {}]",
                totalRevenue, totalCost, totalProfit, completedOrders);

        return RevenueStatisticsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalCost(totalCost)
                .totalProfit(totalProfit)
                .totalCompletedOrders(completedOrders)
                .build();
    }

    /**
     * Get order count by status
     */
    public List<OrderStatusStatisticsResponse> getOrdersByStatus(Instant startDate, Instant endDate) {
        log.info("StatisticsService::getOrdersByStatus - Execution started. [startDate: {}, endDate: {}]",
                startDate, endDate);

        List<Object[]> results = orderRepository.countOrdersByStatus(startDate, endDate);
        List<OrderStatusStatisticsResponse> response = new ArrayList<>();

        // Initialize with all statuses set to 0
        Map<Enums.OrderStatus, Long> statusCountMap = new HashMap<>();
        for (Enums.OrderStatus status : Enums.OrderStatus.values()) {
            statusCountMap.put(status, 0L);
        }

        // Update with actual counts
        for (Object[] row : results) {
            Enums.OrderStatus status = (Enums.OrderStatus) row[0];
            Long count = (Long) row[1];
            statusCountMap.put(status, count);
        }

        // Convert to response list
        for (Map.Entry<Enums.OrderStatus, Long> entry : statusCountMap.entrySet()) {
            response.add(OrderStatusStatisticsResponse.builder()
                    .status(entry.getKey())
                    .count(entry.getValue())
                    .build());
        }

        log.info("StatisticsService::getOrdersByStatus - Execution completed. [statusCount: {}]", response.size());
        return response;
    }

    /**
     * Get completed orders count by order type (ONLINE vs POS)
     */
    public OrderTypeStatisticsResponse getCompletedOrdersByType(Instant startDate, Instant endDate) {
        log.info("StatisticsService::getCompletedOrdersByType - Execution started. [startDate: {}, endDate: {}]",
                startDate, endDate);

        List<Object[]> results = orderRepository.countCompletedOrdersByType(startDate, endDate);

        long onlineOrders = 0L;
        long posOrders = 0L;

        for (Object[] row : results) {
            Enums.OrderType type = (Enums.OrderType) row[0];
            Long count = (Long) row[1];

            if (type == Enums.OrderType.ONLINE) {
                onlineOrders = count;
            } else if (type == Enums.OrderType.POS_INSTORE) {
                posOrders = count;
            }
        }

        log.info("StatisticsService::getCompletedOrdersByType - Execution completed. [online: {}, pos: {}]",
                onlineOrders, posOrders);

        return OrderTypeStatisticsResponse.builder()
                .onlineCompletedOrders(onlineOrders)
                .posCompletedOrders(posOrders)
                .totalCompletedOrders(onlineOrders + posOrders)
                .build();
    }

    /**
     * Get top selling products
     */
    public List<TopSellingProductResponse> getTopSellingProducts(int limit, Instant startDate, Instant endDate) {
        log.info("StatisticsService::getTopSellingProducts - Execution started. [limit: {}]", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> products = productRepository.findTopSellingProducts(pageable);

        // Get revenue by product
        List<Object[]> revenueData = orderDetailRepository.calculateRevenueByProduct(startDate, endDate);
        Map<Long, BigDecimal> revenueMap = new HashMap<>();
        for (Object[] row : revenueData) {
            Long productId = (Long) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            revenueMap.put(productId, revenue);
        }

        List<TopSellingProductResponse> response = new ArrayList<>();
        for (Object[] row : products) {
            Long productId = (Long) row[0];
            String productName = (String) row[1];
            String thumbnail = (String) row[2];
            String brandName = (String) row[3];
            Long soldCount = (Long) row[4];
            BigDecimal revenue = revenueMap.getOrDefault(productId, BigDecimal.ZERO);

            response.add(TopSellingProductResponse.builder()
                    .productId(productId)
                    .productName(productName)
                    .thumbnail(thumbnail)
                    .brandName(brandName)
                    .soldCount(soldCount)
                    .revenue(revenue)
                    .build());
        }

        log.info("StatisticsService::getTopSellingProducts - Execution completed. [count: {}]", response.size());
        return response;
    }

    /**
     * Get out of stock or low stock products
     */
    public List<OutOfStockProductResponse> getOutOfStockProducts(int threshold, int limit) {
        log.info("StatisticsService::getOutOfStockProducts - Execution started. [threshold: {}, limit: {}]",
                threshold, limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = productVariantRepository.findOutOfStockVariants(threshold, pageable);

        List<OutOfStockProductResponse> response = new ArrayList<>();
        for (Object[] row : results) {
            response.add(OutOfStockProductResponse.builder()
                    .variantId((Long) row[0])
                    .productId((Long) row[1])
                    .productName((String) row[2])
                    .sku((String) row[3])
                    .colorName((String) row[4])
                    .sizeName((String) row[5])
                    .materialName((String) row[6])
                    .image((String) row[7])
                    .availableQuantity((Integer) row[8])
                    .build());
        }

        log.info("StatisticsService::getOutOfStockProducts - Execution completed. [count: {}]", response.size());
        return response;
    }

    /**
     * Get top rated products
     */
    public List<TopRatedProductResponse> getTopRatedProducts(int limit) {
        log.info("StatisticsService::getTopRatedProducts - Execution started. [limit: {}]", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = productRepository.findTopRatedProducts(pageable);

        List<TopRatedProductResponse> response = new ArrayList<>();
        for (Object[] row : results) {
            response.add(TopRatedProductResponse.builder()
                    .productId((Long) row[0])
                    .productName((String) row[1])
                    .thumbnail((String) row[2])
                    .brandName((String) row[3])
                    .ratingAverage((BigDecimal) row[4])
                    .ratingCount((Integer) row[5])
                    .build());
        }

        log.info("StatisticsService::getTopRatedProducts - Execution completed. [count: {}]", response.size());
        return response;
    }

    /**
     * Get total inventory value
     */
    public InventoryValueResponse getInventoryValue() {
        log.info("StatisticsService::getInventoryValue - Execution started.");

        List<Object[]> results = productVariantRepository.calculateInventoryValue();

        if (results.isEmpty() || results.get(0) == null) {
            return InventoryValueResponse.builder()
                    .totalInventoryValue(BigDecimal.ZERO)
                    .totalQuantity(0L)
                    .totalVariants(0L)
                    .build();
        }

        Object[] row = results.get(0);
        BigDecimal totalValue = (BigDecimal) row[0];
        Long totalQuantity = ((Number) row[1]).longValue();
        Long totalVariants = (Long) row[2];

        log.info("StatisticsService::getInventoryValue - Execution completed. " +
                "[totalValue: {}, totalQuantity: {}, totalVariants: {}]", totalValue, totalQuantity, totalVariants);

        return InventoryValueResponse.builder()
                .totalInventoryValue(totalValue)
                .totalQuantity(totalQuantity)
                .totalVariants(totalVariants)
                .build();
    }

    /**
     * Get customer statistics
     */
    public CustomerStatisticsResponse getCustomerStatistics() {
        log.info("StatisticsService::getCustomerStatistics - Execution started.");

        Long totalCustomers = userRepository.countTotalCustomers();
        Long activeCustomers = userRepository.countActiveCustomers();

        // Calculate start and end of current month
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfNextMonth = startOfMonth.plusMonths(1);

        Instant startDate = startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endDate = startOfNextMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Long newCustomersThisMonth = userRepository.countNewCustomers(startDate, endDate);

        log.info("StatisticsService::getCustomerStatistics - Execution completed. " +
                        "[total: {}, active: {}, newThisMonth: {}]",
                totalCustomers, activeCustomers, newCustomersThisMonth);

        return CustomerStatisticsResponse.builder()
                .totalCustomers(totalCustomers)
                .activeCustomers(activeCustomers)
                .newCustomersThisMonth(newCustomersThisMonth)
                .build();
    }

    /**
     * Get top customers by revenue
     */
    public List<TopCustomerResponse> getTopCustomers(int limit, Instant startDate, Instant endDate) {
        log.info("StatisticsService::getTopCustomers - Execution started. [limit: {}]", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = orderRepository.findTopCustomersByRevenue(startDate, endDate, pageable);

        List<TopCustomerResponse> response = new ArrayList<>();
        for (Object[] row : results) {
            response.add(TopCustomerResponse.builder()
                    .userId((Long) row[0])
                    .fullName((String) row[1])
                    .phone((String) row[2])
                    .email((String) row[3])
                    .avatar((String) row[4])
                    .totalSpent((BigDecimal) row[5])
                    .orderCount((Long) row[6])
                    .build());
        }

        log.info("StatisticsService::getTopCustomers - Execution completed. [count: {}]", response.size());
        return response;
    }

    /**
     * Get complete dashboard statistics
     */
    public DashboardStatisticsResponse getDashboardStatistics(
            Instant startDate,
            Instant endDate,
            int topProductsLimit,
            int outOfStockLimit,
            int outOfStockThreshold,
            int topCustomersLimit) {

        log.info("StatisticsService::getDashboardStatistics - Execution started.");

        DashboardStatisticsResponse response = DashboardStatisticsResponse.builder()
                .revenue(getRevenueStatistics(startDate, endDate))
                .ordersByStatus(getOrdersByStatus(startDate, endDate))
                .completedOrdersByType(getCompletedOrdersByType(startDate, endDate))
                .topSellingProducts(getTopSellingProducts(topProductsLimit, startDate, endDate))
                .outOfStockProducts(getOutOfStockProducts(outOfStockThreshold, outOfStockLimit))
                .topRatedProducts(getTopRatedProducts(topProductsLimit))
                .inventoryValue(getInventoryValue())
                .customerStatistics(getCustomerStatistics())
                .topCustomers(getTopCustomers(topCustomersLimit, startDate, endDate))
                .build();

        log.info("StatisticsService::getDashboardStatistics - Execution completed.");
        return response;
    }
}
