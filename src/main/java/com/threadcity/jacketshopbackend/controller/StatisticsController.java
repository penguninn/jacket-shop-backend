package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.common.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.statistics.response.*;
import com.threadcity.jacketshopbackend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Get revenue statistics: total revenue, total profit, total cost
     * GET /api/statistics/revenue?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/revenue")
    public ApiResponse<RevenueStatisticsResponse> getRevenueStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("StatisticsController::getRevenueStatistics - Execution started. [startDate: {}, endDate: {}]",
                startDate, endDate);

        Instant startInstant = startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant endInstant = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : null;

        RevenueStatisticsResponse response = statisticsService.getRevenueStatistics(startInstant, endInstant);

        log.info("StatisticsController::getRevenueStatistics - Execution completed.");
        return ApiResponse.<RevenueStatisticsResponse>builder()
                .code(200)
                .message("Get revenue statistics successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Get order count by status
     * GET /api/statistics/orders/by-status?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/orders/by-status")
    public ApiResponse<List<OrderStatusStatisticsResponse>> getOrdersByStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("StatisticsController::getOrdersByStatus - Execution started. [startDate: {}, endDate: {}]",
                startDate, endDate);

        Instant startInstant = startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant endInstant = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : null;

        List<OrderStatusStatisticsResponse> response = statisticsService.getOrdersByStatus(startInstant, endInstant);

        log.info("StatisticsController::getOrdersByStatus - Execution completed.");
        return ApiResponse.<List<OrderStatusStatisticsResponse>>builder()
                .code(200)
                .message("Get orders by status successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Get completed orders count by order type (ONLINE vs POS)
     * GET /api/statistics/orders/by-type?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/orders/by-type")
    public ApiResponse<OrderTypeStatisticsResponse> getCompletedOrdersByType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("StatisticsController::getCompletedOrdersByType - Execution started. [startDate: {}, endDate: {}]",
                startDate, endDate);

        Instant startInstant = startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant endInstant = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : null;

        OrderTypeStatisticsResponse response = statisticsService.getCompletedOrdersByType(startInstant, endInstant);

        log.info("StatisticsController::getCompletedOrdersByType - Execution completed.");
        return ApiResponse.<OrderTypeStatisticsResponse>builder()
                .code(200)
                .message("Get completed orders by type successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Get top selling products
     * GET /api/statistics/products/top-selling?limit=10&startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/products/top-selling")
    public ApiResponse<List<TopSellingProductResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("StatisticsController::getTopSellingProducts - Execution started. [limit: {}]", limit);

        Instant startInstant = startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant endInstant = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : null;

        List<TopSellingProductResponse> response = statisticsService.getTopSellingProducts(limit, startInstant, endInstant);

        log.info("StatisticsController::getTopSellingProducts - Execution completed.");
        return ApiResponse.<List<TopSellingProductResponse>>builder()
                .code(200)
                .message("Get top selling products successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Get out of stock or low stock products
     * GET /api/statistics/products/out-of-stock?threshold=5&limit=20
     */
    @GetMapping("/products/out-of-stock")
    public ApiResponse<List<OutOfStockProductResponse>> getOutOfStockProducts(
            @RequestParam(defaultValue = "0") int threshold,
            @RequestParam(defaultValue = "20") int limit) {

        log.info("StatisticsController::getOutOfStockProducts - Execution started. [threshold: {}, limit: {}]",
                threshold, limit);

        List<OutOfStockProductResponse> response = statisticsService.getOutOfStockProducts(threshold, limit);

        log.info("StatisticsController::getOutOfStockProducts - Execution completed.");
        return ApiResponse.<List<OutOfStockProductResponse>>builder()
                .code(200)
                .message("Get out of stock products successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Get top rated products
     * GET /api/statistics/products/top-rated?limit=10
     */
    @GetMapping("/products/top-rated")
    public ApiResponse<List<TopRatedProductResponse>> getTopRatedProducts(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("StatisticsController::getTopRatedProducts - Execution started. [limit: {}]", limit);

        List<TopRatedProductResponse> response = statisticsService.getTopRatedProducts(limit);

        log.info("StatisticsController::getTopRatedProducts - Execution completed.");
        return ApiResponse.<List<TopRatedProductResponse>>builder()
                .code(200)
                .message("Get top rated products successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Get total inventory value
     * GET /api/statistics/inventory
     */
    @GetMapping("/inventory")
    public ApiResponse<InventoryValueResponse> getInventoryValue() {

        log.info("StatisticsController::getInventoryValue - Execution started.");

        InventoryValueResponse response = statisticsService.getInventoryValue();

        log.info("StatisticsController::getInventoryValue - Execution completed.");
        return ApiResponse.<InventoryValueResponse>builder()
                .code(200)
                .message("Get inventory value successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Get customer statistics
     * GET /api/statistics/customers
     */
    @GetMapping("/customers")
    public ApiResponse<CustomerStatisticsResponse> getCustomerStatistics() {

        log.info("StatisticsController::getCustomerStatistics - Execution started.");

        CustomerStatisticsResponse response = statisticsService.getCustomerStatistics();

        log.info("StatisticsController::getCustomerStatistics - Execution completed.");
        return ApiResponse.<CustomerStatisticsResponse>builder()
                .code(200)
                .message("Get customer statistics successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Get top customers by revenue
     * GET /api/statistics/customers/top?limit=10&startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/customers/top")
    public ApiResponse<List<TopCustomerResponse>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("StatisticsController::getTopCustomers - Execution started. [limit: {}]", limit);

        Instant startInstant = startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant endInstant = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : null;

        List<TopCustomerResponse> response = statisticsService.getTopCustomers(limit, startInstant, endInstant);

        log.info("StatisticsController::getTopCustomers - Execution completed.");
        return ApiResponse.<List<TopCustomerResponse>>builder()
                .code(200)
                .message("Get top customers successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Get complete dashboard statistics (all in one call)
     * GET /api/statistics/dashboard?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/dashboard")
    public ApiResponse<DashboardStatisticsResponse> getDashboardStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int topProductsLimit,
            @RequestParam(defaultValue = "10") int outOfStockLimit,
            @RequestParam(defaultValue = "5") int outOfStockThreshold,
            @RequestParam(defaultValue = "10") int topCustomersLimit) {

        log.info("StatisticsController::getDashboardStatistics - Execution started. [startDate: {}, endDate: {}]",
                startDate, endDate);

        Instant startInstant = startDate != null ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
        Instant endInstant = endDate != null ? endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant() : null;

        DashboardStatisticsResponse response = statisticsService.getDashboardStatistics(
                startInstant,
                endInstant,
                topProductsLimit,
                outOfStockLimit,
                outOfStockThreshold,
                topCustomersLimit
        );

        log.info("StatisticsController::getDashboardStatistics - Execution completed.");
        return ApiResponse.<DashboardStatisticsResponse>builder()
                .code(200)
                .message("Get dashboard statistics successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }
}
