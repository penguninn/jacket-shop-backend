package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.SaleRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.SaleResponse;
import com.threadcity.jacketshopbackend.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Slf4j
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ApiResponse<?> applySale(@Valid @RequestBody SaleRequest request) {
        log.info("SaleController::applySale - Execution started.");
        SaleResponse response = saleService.applySale(request);
        log.info("SaleController::applySale - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Sale applied successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getSale(@PathVariable Long id) {
        log.info("SaleController::getSale - Execution started. [id: {}]", id);
        var response = saleService.getSale(id);
        log.info("SaleController::getSale - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Get sale successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping
    public ApiResponse<?> getAllSales() {
        log.info("SaleController::getAllSales - Execution started.");
        var response = saleService.getAllSales();
        log.info("SaleController::getAllSales - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Get all sales successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> removeSale(@PathVariable Long id) {
        log.info("SaleController::removeSale - Execution started. [id: {}]", id);
        saleService.removeSale(id);
        log.info("SaleController::removeSale - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Sale removed successfully.")
                .timestamp(Instant.now())
                .build();
    }
}