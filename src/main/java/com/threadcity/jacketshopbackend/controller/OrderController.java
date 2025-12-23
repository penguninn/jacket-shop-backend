package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.OrderHistoryResponse;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<?> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("OrderController::createOrder - Execution started.");
        OrderResponse response = orderService.createOrder(request);
        log.info("OrderController::createOrder - Execution completed.");
        return ApiResponse.builder()
                .code(201)
                .message("Order created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<?> getMyOrders(@RequestParam(required = false) OrderStatus status) {
        log.info("OrderController::getMyOrders - Execution started.");
        List<OrderResponse> response = orderService.getMyOrders(status);
        log.info("OrderController::getMyOrders - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Get my orders successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getOrderById(@PathVariable Long id) {
        log.info("OrderController::getOrderById - Execution started. [id: {}]", id);
        OrderResponse response = orderService.getOrderById(id);
        log.info("OrderController::getOrderById - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get order by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}/history")
    public ApiResponse<?> getOrderHistory(@PathVariable Long id) {
        log.info("OrderController::getOrderHistory - Execution started. [id: {}]", id);
        List<OrderHistoryResponse> response = orderService.getOrderHistory(id);
        log.info("OrderController::getOrderHistory - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get order history successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<?> cancelOrder(@PathVariable Long id) {
        log.info("OrderController::cancelOrder - Execution started. [id: {}]", id);
        OrderResponse response = orderService.cancelOrder(id);
        log.info("OrderController::cancelOrder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Order cancelled successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }
    
    @PutMapping("/{id}/receive")
    public ApiResponse<?> receiveOrder(@PathVariable Long id) {
        log.info("OrderController::receiveOrder - Execution started. [id: {}]", id);
        OrderResponse response = orderService.receiveOrder(id);
        log.info("OrderController::receiveOrder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Order received successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }
    
    @PostMapping("/{id}/return")
    public ApiResponse<?> requestReturn(@PathVariable Long id, @RequestBody(required = false) String reason) {
        log.info("OrderController::requestReturn - Execution started. [id: {}]", id);
        OrderResponse response = orderService.requestReturn(id, reason);
        log.info("OrderController::requestReturn - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Return requested successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }
    
    @PostMapping("/{id}/reorder")
    public ApiResponse<?> reorder(@PathVariable Long id) {
        log.info("OrderController::reorder - Execution started. [id: {}]", id);
        orderService.reorder(id);
        log.info("OrderController::reorder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Items added to cart successfully.")
                .data(null)
                .timestamp(Instant.now())
                .build();
    }
}
