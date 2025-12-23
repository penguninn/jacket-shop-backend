package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.filter.OrderFilterRequest;
import com.threadcity.jacketshopbackend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<?> getAllOrders(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) OrderType orderType,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("AdminOrderController::getAllOrders - Execution started.");
        OrderFilterRequest request = new OrderFilterRequest();
        request.setOrderCode(orderCode);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setOrderType(orderType);
        request.setStatus(status);
        request.setPaymentStatus(paymentStatus);
        request.setUserId(userId);
        request.setStaffId(staffId);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDir(sortDir);

        PageResponse<?> response = orderService.getAllOrders(request);
        log.info("AdminOrderController::getAllOrders - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Get all orders successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getOrderById(@PathVariable Long id) {
        log.info("AdminOrderController::getOrderById - Execution started. [id: {}]", id);
        var response = orderService.getOrderById(id);
        log.info("AdminOrderController::getOrderById - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get order by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/payment-status")
    public ApiResponse<?> updatePaymentStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
        log.info("AdminOrderController::updatePaymentStatus - Execution started. [id: {}]", id);
        var response = orderService.updatePaymentStatus(id, status);
        log.info("AdminOrderController::updatePaymentStatus - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Payment status updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/shipping-info")
    public ApiResponse<?> updateShippingInfo(@PathVariable Long id, @RequestParam String carrierName,
            @RequestParam String carrierCode) {
        log.info("AdminOrderController::updateShippingInfo - Execution started. [id: {}]", id);
        var response = orderService.updateShippingInfo(id, carrierName, carrierCode);
        log.info("AdminOrderController::updateShippingInfo - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Shipping info updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<?> confirmOrder(@PathVariable Long id) {
        log.info("AdminOrderController::confirmOrder - Execution started. [id: {}]", id);
        var response = orderService.confirmOrder(id);
        log.info("AdminOrderController::confirmOrder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Order confirmed successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/ship")
    public ApiResponse<?> shipOrder(@PathVariable Long id) {
        log.info("AdminOrderController::shipOrder - Execution started. [id: {}]", id);
        var response = orderService.shipOrder(id);
        log.info("AdminOrderController::shipOrder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Order shipped successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<?> completeOrder(@PathVariable Long id) {
        log.info("AdminOrderController::completeOrder - Execution started. [id: {}]", id);
        var response = orderService.completeOrder(id);
        log.info("AdminOrderController::completeOrder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Order completed successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<?> cancelOrder(@PathVariable Long id) {
        log.info("AdminOrderController::cancelOrder - Execution started. [id: {}]", id);
        var response = orderService.cancelOrder(id);
        log.info("AdminOrderController::cancelOrder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Order cancelled successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }
}
