package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
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

    @GetMapping("/my-orders")
    public ApiResponse<?> getMyOrders() {
        log.info("OrderController::getMyOrders - Execution started.");
        List<OrderResponse> response = orderService.getMyOrders();
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
}
