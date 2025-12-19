package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.SaleRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.SaleResponse;
import com.threadcity.jacketshopbackend.filter.SaleFilterRequest;
import com.threadcity.jacketshopbackend.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Slf4j
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    public ApiResponse<?> getAllSales(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            @RequestParam(required = false) BigDecimal minDiscount,
            @RequestParam(required = false) BigDecimal maxDiscount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        log.info("SaleController::getAllSales - Execution started.");
        SaleFilterRequest request = SaleFilterRequest.builder()
                .search(search)
                .fromDate(fromDate)
                .toDate(toDate)
                .minDiscount(minDiscount)
                .maxDiscount(maxDiscount)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();
        
        PageResponse<List<SaleResponse>> response = saleService.getAllSales(request);
        log.info("SaleController::getAllSales - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Get all sales successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getSaleById(@PathVariable Long id) {
        log.info("SaleController::getSaleById - Execution started. [id: {}]", id);
        SaleResponse response = saleService.getSaleById(id);
        log.info("SaleController::getSaleById - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Get sale successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createSale(@Valid @RequestBody SaleRequest request) {
        log.info("SaleController::createSale - Execution started.");
        SaleResponse response = saleService.createSale(request);
        log.info("SaleController::createSale - Execution completed.");
        return ApiResponse.builder()
                .code(201)
                .message("Sale created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateSale(@PathVariable Long id, @Valid @RequestBody SaleRequest request) {
        log.info("SaleController::updateSale - Execution started. [id: {}]", id);
        SaleResponse response = saleService.updateSale(id, request);
        log.info("SaleController::updateSale - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Sale updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteSale(@PathVariable Long id) {
        log.info("SaleController::deleteSale - Execution started. [id: {}]", id);
        saleService.deleteSale(id);
        log.info("SaleController::deleteSale - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Sale deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/bulk/delete")
    public ApiResponse<?> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        log.info("SaleController::bulkDelete - Execution started.");
        saleService.bulkDeleteSales(request);
        log.info("SaleController::bulkDelete - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Bulk delete sales successfully.")
                .timestamp(Instant.now())
                .build();
    }
}
