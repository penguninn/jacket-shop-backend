package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.filter.ShippingMethodsFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.ShippingMethodsRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ShippingMethodsResponse;
import com.threadcity.jacketshopbackend.service.ShippingMethodsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/shipping-methods")
@RequiredArgsConstructor
@Slf4j
public class ShippingMethodsController {

    private final ShippingMethodsService shippingMethodService;

    @GetMapping
    public ApiResponse<?> getAllShippingMethods(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        log.info("ShippingMethodController::getAllShippingMethods - Execution started");

        ShippingMethodsFilterRequest request = ShippingMethodsFilterRequest.builder()
                .search(search)
                .status(status)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<?> pageResponse = shippingMethodService.getAllShippingMethods(request);

        log.info("ShippingMethodController::getAllShippingMethods - Execution completed");

        return ApiResponse.builder()
                .code(200)
                .message("Get all shipping methods successfully.")
                .data(pageResponse)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getShippingMethodById(@PathVariable Long id) {
        log.info("ShippingMethodController::getShippingMethodById - Execution started. [id: {}]", id);

        ShippingMethodsResponse response = shippingMethodService.getShippingMethodById(id);

        log.info("ShippingMethodController::getShippingMethodById - Execution completed. [id: {}]", id);

        return ApiResponse.builder()
                .code(200)
                .message("Get shipping method by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createShippingMethod(@RequestBody ShippingMethodsRequest request) {
        log.info("ShippingMethodController::createShippingMethod - Execution started.");

        ShippingMethodsResponse response = shippingMethodService.createShippingMethod(request);

        log.info("ShippingMethodController::createShippingMethod - Execution completed.");

        return ApiResponse.builder()
                .code(201)
                .message("Shipping method created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateShippingMethod(
            @PathVariable Long id,
            @RequestBody ShippingMethodsRequest request
    ) {
        log.info("ShippingMethodController::updateShippingMethod - Execution started. [id: {}]", id);

        ShippingMethodsResponse response = shippingMethodService.updateShippingMethod(id, request);

        log.info("ShippingMethodController::updateShippingMethod - Execution completed. [id: {}]", id);

        return ApiResponse.builder()
                .code(200)
                .message("Shipping method updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteShippingMethod(@PathVariable Long id) {
        log.info("ShippingMethodController::deleteShippingMethod - Execution started. [id: {}]", id);

        shippingMethodService.deleteShippingMethod(id);

        log.info("ShippingMethodController::deleteShippingMethod - Execution completed. [id: {}]", id);

        return ApiResponse.builder()
                .code(200)
                .message("Shipping method deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }
}
